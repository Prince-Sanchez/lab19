package application;

import java.sql.Connection;
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

		/*
		 * valid pharmacy name and address, get pharmacy id and phone
		 */
		// TODO 
		
		// TODO find the patient information 

		// TODO find the prescription 


		/*
		 * have we exceeded the number of allowed refills
		 * the first fill is not considered a refill.
		 */
		
		// TODO 
		
		/*
		 * get doctor information 
		 */
		// TODO

		/*
		 * calculate cost of prescription
		 */
		// TODO 
		
		// TODO save updated prescription 

		// show the updated prescription with the most recent fill information
		model.addAttribute("message", "Prescription filled.");
		model.addAttribute("prescription", p);
		return "prescription_show";
	}
	
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}