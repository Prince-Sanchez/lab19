package application;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

@Controller
public class ControllerPrescriptionCreate {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 * Doctor requests blank form for new prescription.
	 */
	@GetMapping("/prescription/new")
	public String getPrescriptionForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_create";
	}

	// process data entered on prescription_create form
	@PostMapping("/prescription")
	public String createPrescription(PrescriptionView p, Model model) throws SQLException {

		System.out.println("createPrescription " + p);

		try (Connection con = getConnection();){
			String state = "insert into prescription (patient_id, doctor_id, drug_name, quantity, patientFirstName," +
					"patientLastName, doctorFirstName, doctorLastName, dateCreated, num_refills) values(?,?,?,?,?,?,?,?,?,?) ";
			PreparedStatement ps = con.prepareStatement(state, Statement.RETURN_GENERATED_KEYS);

			PreparedStatement ps2 = con.prepareStatement("select * from patient where last_name =? and patient_id=?");// VALIDATE Patient FROM LAST NAME and ID
			ps2.setString(1, p.getPatientLastName());
			ps2.setInt(2, p.getPatient_id());
			ResultSet rs2 = ps2.executeQuery();
			if (rs2.next()){
				System.out.println("PATIENT IS FOUND"); // patient confirmed if ID is found
			}
			else {
				model.addAttribute("message", "Patient not found."); // error message if no doctor ID found
				model.addAttribute("prescription", p);
				return "prescription_create"; // returns to edit
			}// END OF VALIDATE PATIENT
			ps.setInt(1, p.getPatient_id());

			ps2 = con.prepareStatement("select * from doctor where last_name =? and id=?");// VALIDATE doctor FROM LAST NAME and ID
			ps2.setString(1, p.getDoctorLastName());
			ps2.setInt(2, p.getDoctor_id());
			rs2 = ps2.executeQuery();
			if (rs2.next()){
				System.out.println("Doctor IS FOUND"); // doctor confirmed if ID is found
			}
			else {
				model.addAttribute("message", "Doctor not found."); // error message if no doctor ID found
				model.addAttribute("prescription", p);
				return "prescription_create"; // returns to edit
			}// END OF VALIDATE doctor
			ps.setInt(2, p.getDoctor_id());

			ps2 = con.prepareStatement("select * from drug where drug_name=?");// VALIDATE drug FROM drug name
			ps2.setString(1, p.getDrugName());
			rs2 = ps2.executeQuery();
			if (rs2.next()){
				System.out.println("drug IS FOUND"); // drug confirmed if DrugName is found
			}
			else {
				model.addAttribute("message", "drug not found."); // error message if no drug ID found
				model.addAttribute("prescription", p);
				return "prescription_create"; // returns to edit
			}// END OF VALIDATE drug
			ps.setString(3, p.getDrugName());
			ps.setInt(4, p.getQuantity());
			ps.setString(5, p.getPatientFirstName());
			ps.setString(6, p.getPatientLastName());
			ps.setString(7, p.getDoctorFirstName());
			ps.setString(8, p.getDoctorLastName());
			ps.setString(9, LocalDate.now().toString());
			ps.setInt(10, p.getRefills());


			ps.executeUpdate(); // insert into table
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) p.setRxid(rs.getInt(1)); // sets RXID

			PreparedStatement ps3 = con.prepareStatement("SELECT p.pharmacy_id, c.price FROM Pharmacy p JOIN Cost c ON p.pharmacy_id = c.pharmacy_id WHERE c.drug_id = ?");
			ps3.setInt(1, rs2.getInt("drug_id"));
			ResultSet rs3 = ps3.executeQuery();
			if (rs3.next()) {
				int pharmacyId = rs3.getInt("pharmacy_id");
				BigDecimal price = rs3.getBigDecimal("price");
				PreparedStatement ps4 = con.prepareStatement("INSERT INTO Refills (rxid, pharmacy_id, dateFilled, cost, refills, refillsRemaining) VALUES (?, ?, ?, ?, ?, ?)");
				ps4.setInt(1, p.getRxid());
				ps4.setInt(2, pharmacyId);
				ps4.setDate(3, new java.sql.Date(System.currentTimeMillis())); // for current date
				ps4.setBigDecimal(4, price);
				ps4.setInt(5, p.getRefills());
				ps4.setInt(6, p.getRefillsRemaining());

				ps4.executeUpdate();
				// Now you have linked the prescription with a pharmacy and have a cost associated with it
				model.addAttribute("pharmacyId", pharmacyId);
			}
			PreparedStatement ps5 = con.prepareStatement("SELECT r.*, p.pharmacy_id, p.pharmacyName, p.pharmacyAddress, p.pharmacyPhone FROM Refills r JOIN Pharmacy p ON r.pharmacy_id = p.pharmacy_id WHERE r.rxid = ?");
			ps5.setInt(1, p.getRxid()); // Using the rxid from the prescription you just inserted
			ResultSet rs5 = ps5.executeQuery();

			if (rs5.next()) {
				p.setPharmacyID(rs5.getInt("pharmacy_id"));
				p.setPharmacyName(rs5.getString("pharmacyName"));
				p.setPharmacyAddress(rs5.getString("pharmacyAddress"));
				p.setPharmacyPhone(rs5.getString("pharmacyPhone"));
				p.setCost(rs5.getString("cost"));
				p.setDateFilled(rs5.getString("dateFilled"));
			}

			model.addAttribute("message", "Prescription created.");
			model.addAttribute("prescription", p);
			return "prescription_show";


		} catch (SQLException e) {
			model.addAttribute("message", "Prescription could not be registered because of " + e);
			model.addAttribute("prescription", p);
			return "prescription_create";
		}
	}

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
