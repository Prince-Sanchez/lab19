package application;

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
			String state = "insert into prescription (patient_id, doctor_id, drugName, quantity, patientFirstName," +
					"patientLastName, doctorFirstName, doctorLastName, dateCreated) values(?,?,?,?,?,?,?,?,?) ";
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

			ps2 = con.prepareStatement("select * from drug where name=?");// VALIDATE drug FROM drug name
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
			ps.setString(6,p.getPatientFirstName());
			ps.setString(7, p.getDoctorFirstName());
			ps.setString(8, p.getDoctorLastName());
			ps.setString(9, LocalDate.now().toString());

			ps.executeUpdate(); // insert into table
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) p.setRxid(rs.getInt(1)); // sets RXID

			model.addAttribute("message", "Prescription created.");
			model.addAttribute("prescription", p);
			return "prescription_show";

		} catch (SQLException e) {
			model.addAttribute("message", "Prescription could not be registered because of " + e);
			model.addAttribute("prescription", p);
			return "prescription_create";
		}

		/*
		 * insert prescription
		 */
		//TODO

	}

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
