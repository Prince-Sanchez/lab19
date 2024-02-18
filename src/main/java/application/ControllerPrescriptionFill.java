package application;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

import javax.xml.transform.Result;

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

		try (Connection con = getConnection()) {
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
			String sqlPatient = "select patient_id, first_name from Patient where last_name = ?";
					//"SELECT p.patient_id FROM Patient p JOIN Prescription r ON p.patient_id = r.patient_id WHERE r.patientFirstName = ?";
			PreparedStatement psPatient = con.prepareStatement(sqlPatient);
			psPatient.setString(1, p.getPatientLastName());
			ResultSet rsPatient = psPatient.executeQuery();
			if (rsPatient.next()){
				System.out.println("PATIENT IS FOUND");
				p.setPatient_id(rsPatient.getInt("patient_id"));
				p.setPatientFirstName(rsPatient.getString("first_name"));
			}
			else {
				model.addAttribute("message", "Patient not found!!!.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			// Finding the prescription
			String sqlPrescription = "SELECT rxid, num_refills, quantity, drug_name FROM Prescription WHERE rxid = ?";
			PreparedStatement psPrescription = con.prepareStatement(sqlPrescription);
			psPrescription.setInt(1, p.getRxid());
			ResultSet rsPrescription = psPrescription.executeQuery();
			if (rsPrescription.next()){
				System.out.println("PRESCRIPTION IS FOUND");
				p.setRxid(rsPrescription.getInt("rxid"));
				p.setRefills(rsPrescription.getInt("num_refills"));
				p.setQuantity(rsPrescription.getInt("quantity"));
				p.setDrugName((rsPrescription.getString("drug_name")));
			}
			else {
				model.addAttribute("message", "PRESCRIPTION NOT FOUND.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			String sqlDrugID = "select drug_id from drug where drug_name = ?";
			PreparedStatement psDrug = con.prepareStatement((sqlDrugID));
			psDrug.setString(1, p.getDrugName());
			ResultSet rsDrugID = psDrug.executeQuery();
			Integer drugID = null;
			if(rsDrugID.next()){
				drugID = rsDrugID.getInt("drug_id");
			}





			// Getting the number of refills allowed for the prescription
			int numRefillsAllowed;
			String sqlRefills = "SELECT p.num_refills, r.dateFilled FROM Prescription p  join Refills r on p.rxid = r.rxid WHERE r.rxid = ?";
			PreparedStatement psPrescriptionRefills = con.prepareStatement(sqlRefills);
			psPrescriptionRefills.setInt(1, p.getRxid());
			ResultSet rsPrescriptionRefills = psPrescriptionRefills.executeQuery();
			if (rsPrescriptionRefills.next()) {
				numRefillsAllowed = rsPrescriptionRefills.getInt("num_refills");
				Date refillDate = rsPrescriptionRefills.getDate("dateFilled");
				if(refillDate == null){
					String sqlInitialRefill = "update Refills set dateFilled = CURRENT_DATE where rxid = ?";
					PreparedStatement psRefillDate = con.prepareStatement(sqlInitialRefill);
					psRefillDate.setInt(1,p.getRxid());
					psRefillDate.executeUpdate();
				}else if(numRefillsAllowed > 0){
					String sqlFinalRefill = "update Refills rf join Prescription p on rf.rxid = p.rxid set rf.dateFilled = CURRENT_DATE, p.num_refills  = p.num_refills - 1 where rf.rxid = ?";
					PreparedStatement psRefillTime = con.prepareStatement(sqlFinalRefill);
					psRefillTime.setInt(1,p.getRxid());
					psRefillTime.executeUpdate();
				}
			} else {
				model.addAttribute("message", "THERE ARE NO REFILLS FOR THIS PRESCRIPTION.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}




			// Get doctor information
			String sqlDoctor = "SELECT doctor_id from Prescription where rxid = ?"; // changed * to id
			PreparedStatement psDoctor = con.prepareStatement(sqlDoctor);
			psDoctor.setInt(1, p.getRxid());
			ResultSet rsDoctor = psDoctor.executeQuery();
			if (rsDoctor.next()) {
				p.setDoctor_id(rsDoctor.getInt("doctor_id"));
			}else{
				model.addAttribute("message", "DOCTOR INFO NOT FOUND!");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}
			String sqlDoctorName = "select first_name, last_name from Doctor where id = ?";
			PreparedStatement psDocName = con.prepareStatement(sqlDoctorName);
			psDocName.setInt(1,p.getDoctor_id());
			ResultSet rsDoctorName = psDocName.executeQuery();
			if(rsDoctorName.next()){
				p.setDoctorFirstName(rsDoctorName.getString("first_name"));
				p.setDoctorLastName(rsDoctorName.getString("last_name"));
			}




			// Get the price of the drug
			String drug_name = p.getDrugName(); // Get the drug name from the prescription
			int pharmacyId = p.getPharmacyID(); // Get the pharmacy ID
			int quantity = p.getQuantity(); // Get the quantity of the drug for the refill
			String sqlGetPrice = "SELECT c.price FROM Cost c WHERE c.pharmacy_id = ? AND c.drug_id = ? and c.quantity = ?";
					//"SELECT c.price FROM Cost c JOIN Drug d ON c.drug_id = d.drug_id WHERE d.drug_name = ? AND c.drug_id = ?";
			PreparedStatement psGetPrice = con.prepareStatement(sqlGetPrice);
			psGetPrice.setInt(2, drugID);
			psGetPrice.setInt(1, pharmacyId);
			psGetPrice.setInt(3, quantity);
			ResultSet rsPrice = psGetPrice.executeQuery();
			if (rsPrice.next()) {
				BigDecimal price = rsPrice.getBigDecimal("price");

				String priceString = price.toString(); // Converts the BigDecimal to a String.
				p.setCost(priceString); // Assuming setCost() accepts a String.
				double totalCost = price.doubleValue() * quantity; // Use doubleValue() for calculations.
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
				model.addAttribute("message", "Failed to get price for the drug.");
				model.addAttribute("prescription", p);
				return "prescription_fill";
			}

			// Save updated prescription
//			int refillsRemaining = numRefillsAllowed - refillsUsed;
//			String sqlUpdatePrescription = "UPDATE Prescription SET num_refills = ? WHERE rxid = ?";
//			PreparedStatement psUpdatePrescription = con.prepareStatement(sqlUpdatePrescription);
//			psUpdatePrescription.setInt(1, refillsRemaining);
//			psUpdatePrescription.setInt(2, p.getRxid());
//			int affectedRows = psUpdatePrescription.executeUpdate();
//			if (affectedRows > 0) {
//				// The update was successful, proceed with the refill process
//				con.commit(); // Commit the transaction
//				model.addAttribute("message", "Prescription updated with new refill count.");
//
//			} else {
//				// The update failed, handle this case appropriately
//				con.rollback(); // Rollback the transaction
//				model.addAttribute("message", "Failed to update prescription with new refill count.");
//				return "prescription_fill";
//			}
			p.setDateFilled(LocalDate.now().toString());
			model.addAttribute("message", "Prescription updated with new refill count.");
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