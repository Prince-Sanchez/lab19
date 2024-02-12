package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
	public String createPrescription(PrescriptionView p, Model model) {
		try(Connection con = getConnection();){
			String sql = "INSERT INTO Prescription (patient_id, doctor_id, rx_id, date) VALUES (?, ?, ?, CURDATE())";
			try(PreparedStatement ps = con.prepareStatement(sql)){
				ps.setInt(1, p.getPatient_id());
				ps.setInt(2, p.getDoctor_id());
				ps.setInt(3, p.getRxid());

				int result = ps.executeUpdate();
				if (result > 0) {
					model.addAttribute("message", "Prescription created successfully.");
				} else {
					model.addAttribute("message", "Failed to create prescription.");
				}
			}
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
		}
		return "prescription_show"; //this redirects us to the appropriate view
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
