package application;

import java.sql.Connection;
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

		System.out.println("createPrescription " + p);

		/*
		 * valid doctor name and id
		 */
		//TODO

		/*
		 * valid patient name and id
		 */
		//TODO

		/*
		 * valid drug name
		 */
		//TODO

		/*
		 * insert prescription  
		 */
		//TODO 
		

		model.addAttribute("message", "Prescription created.");
		model.addAttribute("prescription", p);
		return "prescription_show";
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
