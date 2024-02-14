package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

@Controller
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Patient requests form to fill prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_fill";
	}


	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model) {

		try (Connection con = getConnection();) {
			con.setAutoCommit(false); // Start transaction

			// Validating pharmacy name and address, and getting pharmacy id and phone
			String sqlPharmacy = "SELECT pharmacy_id, pharmacyPhone FROM Pharmacy WHERE pharmacyName = ? AND pharmacyAddress = ?";
			PreparedStatement psPharmacy = con.prepareStatement(sqlPharmacy);
				psPharmacy.setString(1, p.getPharmacyName());
				psPharmacy.setString(2, p.getPharmacyAddress());
				ResultSet rsPharmacy = psPharmacy.executeQuery();
					if (rsPharmacy.next()) {
						System.out.println("PHARMACY IS FOUND");
						p.setPharmacyID(rsPharmacy.getInt("pharmacy_id"));
						p.setPharmacyPhone(rsPharmacy.getString("pharmacyPhone"));
					}else {
						model.addAttribute("message", "Pharmacy not found.");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}

			// Finding the patient information
			String sqlPatient = "SELECT p.patient_id FROM Patient p JOIN Prescription r ON p.patient_id = r.patient_id WHERE r.patientLastName = ?";
			PreparedStatement psPatient = con.prepareStatement(sqlPatient);
				psPatient.setString(1, p.getPatientLastName());
				ResultSet rsPatient = psPatient.executeQuery();
					if (rsPatient.next()){
						System.out.println("PATIENT IS FOUND");
						p.setPatient_id(rsPatient.getInt("patient_id"));
					}
					else {
						model.addAttribute("message", "Patient not found!!!.");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}

			// Finding the prescription
			String sqlPrescription = "SELECT * FROM Prescription WHERE rxid = ?";
			PreparedStatement psPrescription = con.prepareStatement(sqlPrescription);
				psPrescription.setInt(1, p.getRxid());
				ResultSet rsPrescription = psPrescription.executeQuery();
					if (rsPrescription.next()){
						System.out.println("PRESCRIPTION IS FOUND");
						p.setRxid(rsPrescription.getInt("rxid"));
						p.setDrugName(rsPrescription.getString("drug_name"));
						p.setQuantity(rsPrescription.getInt("quantity"));
						p.setPatient_id(rsPrescription.getInt("patient_id"));
						p.setDoctor_id(rsPrescription.getInt("doctor_id"));
						p.setDateCreated(rsPrescription.getString("dateCreated"));
						p.setRefills(rsPrescription.getInt("num_refills"));
						p.setDoctorFirstName(rsPrescription.getString("doctorFirstName"));
						p.setDoctorLastName(rsPrescription.getString("doctorLastName"));
						p.setPatientFirstName(rsPrescription.getString("patientFirstName"));
						p.setPatientLastName(rsPrescription.getString("patientLastName"));
					}
					else {
						model.addAttribute("message", "Prescription not found.");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}

			// Getting the number of refills allowed for the prescription
			int numRefillsAllowed = 0;
			String sqlRefills = "SELECT num_refills FROM Prescription WHERE rxid = ?";
			PreparedStatement psPrescriptionRefills = con.prepareStatement(sqlRefills);
				psPrescriptionRefills.setInt(1, p.getRxid());
				ResultSet rsPrescriptionRefills = psPrescription.executeQuery();
					if (rsPrescriptionRefills.next()) {
						numRefillsAllowed = rsPrescriptionRefills.getInt("num_refills");
					} else {
						model.addAttribute("message", "Prescription not found.");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}

			// Counting the number of refills that have been used
			int refillsUsed = 0;
			String sqlRefillsCount = "SELECT COUNT(*) AS refill_count FROM Refills WHERE rxid = ?";
			PreparedStatement psRefillsCount = con.prepareStatement(sqlRefillsCount);
				psRefillsCount.setInt(1, p.getRxid());
				ResultSet rsRefillsCount = psRefillsCount.executeQuery();
					if (rsRefillsCount.next()) {
						refillsUsed = rsRefillsCount.getInt("refill_count");
					}else {
						model.addAttribute("message", "Prescription not found.");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}


			// Get doctor information
			String sqlDoctor = "SELECT * FROM Doctor WHERE id = ?";
			PreparedStatement psDoctor = con.prepareStatement(sqlDoctor);
				psDoctor.setInt(1, p.getDoctor_id());
				ResultSet rsDoctor = psDoctor.executeQuery();
					if (rsDoctor.next()) {
						return "prescription_show";
					}


			String drug_name = p.getDrugName(); // Get the drug name from the prescription
			int pharmacyId = p.getPharmacyID(); // Get the pharmacy ID
			int quantity = p.getQuantity(); // Get the quantity of the drug for the refill
			String sqlGetPrice = "SELECT c.price FROM Cost c JOIN Drug d ON c.drug_id = d.drug_id WHERE d.drug_name = ? AND c.pharmacy_id = ?";
			PreparedStatement psGetPrice = con.prepareStatement(sqlGetPrice);
				psGetPrice.setString(1, drug_name);
				psGetPrice.setInt(2, pharmacyId);
				ResultSet rsPrice = psGetPrice.executeQuery();
					if (rsPrice.next()) {
						double price = rsPrice.getDouble("price");
						double totalCost = price * quantity; // Calculate the total cost
						// Inserting refill information into refills table
						String sqlInsertRefill = "INSERT INTO Refills (rxid, pharmacy_id, dateFilled, cost, refills, refillsRemaining) VALUES (?, ?, ?, ?, ?, ?)";
						PreparedStatement psInsertRefill = con.prepareStatement(sqlInsertRefill);
							psInsertRefill.setInt(1, p.getRxid());
							psInsertRefill.setInt(2, pharmacyId);
							psInsertRefill.setDate(3, new java.sql.Date(System.currentTimeMillis())); // Current date for dateFilled
							psInsertRefill.setDouble(4, totalCost);
							psInsertRefill.setInt(5, p.getRefills());
							psInsertRefill.setInt(6, p.getRefillsRemaining());

							int affectedRows = psInsertRefill.executeUpdate();
							if (affectedRows == 0) {
								model.addAttribute("message", "Inserting not done.");
								model.addAttribute("prescription", p);
								return "prescription_fill";
							}
					} else {
						model.addAttribute("message", "Prescription not found.");
						model.addAttribute("prescription", p);
						return "prescription_fill";
					}


			// Save updated prescription
			int refillsRemaining = numRefillsAllowed - refillsUsed;
			String sqlUpdatePrescription = "UPDATE Refills SET refillsRemaining = ? WHERE rxid = ? AND dateFilled = (SELECT MAX(dateFilled) FROM Refills WHERE rxid = ?)";
			PreparedStatement psUpdatePrescription = con.prepareStatement(sqlUpdatePrescription);
				psUpdatePrescription.setInt(1, refillsRemaining); // Increment the number of refills used
				psUpdatePrescription.setInt(2, p.getRxid());
				psUpdatePrescription.setInt(3, p.getRxid());
				int affectedRows = psUpdatePrescription.executeUpdate();
				if (affectedRows > 0) {
					p.setRefillsRemaining(refillsRemaining);
				}else {
					model.addAttribute("message", "Prescription not found.");
					model.addAttribute("prescription", p);
					return "prescription_fill";
				}

			// Checking if the number of used refills is greater than or equal to the allowed number
			if (refillsUsed >= numRefillsAllowed) {
				model.addAttribute("message", "Prescription not found.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			con.commit(); // Commit transaction if everything was successful
			// Show the updated prescription with the most recent fill information
			model.addAttribute("message", "Prescription filled.");
			model.addAttribute("prescription", p);
			return "prescription_show";

		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
			model.addAttribute("prescription", p);
			return "prescription_fill";
		}
	}


	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}