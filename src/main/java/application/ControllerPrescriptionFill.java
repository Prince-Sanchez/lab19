package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

	// process data from prescription_fill form
	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model) {
		try (Connection con = getConnection();) {
			String sql = "UPDATE Prescription SET fill_date = CURDATE() WHERE rx_id = ? AND patient_last_name = ?";
			try (PreparedStatement ps = con.prepareStatement(sql)) {
				ps.setInt(1, p.getRxid());
				ps.setString(2, p.getPatientLastName());

				int result = ps.executeUpdate();
				if (result > 0) {
					model.addAttribute("message", "Prescription filled successfully.");
				} else {
					model.addAttribute("message", "Failed to fill prescription.");
				}
			}
		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error: " + e.getMessage());
		}
		return "prescription_show"; // this redirects us to the appropriate view
	}


	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}