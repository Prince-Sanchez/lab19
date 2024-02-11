package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import view.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientUpdate {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) throws SQLException {

		PatientView p = new PatientView();
		p.setId(id);
		try (Connection con = getConnection();){

			PreparedStatement ps = con.prepareStatement("select first_name, last_name, street, city, state, zipcode, birthdate, primaryName " +
					"from patient where patient_id=?");// search for patient by id
			ps.setInt(1,id);

			ResultSet rs = ps.executeQuery(); // retrieve patient data
			if((rs.next())){ // insert patient into p
				p.setFirst_name(rs.getString(1));
				p.setLast_name(rs.getString(2));
				p.setStreet(rs.getString(3));
				p.setCity(rs.getString(4));
				p.setState(rs.getString(5));
				p.setZipcode(rs.getString(6));
				p.setBirthdate(rs.getString(7));
				p.setPrimaryName(rs.getString(8));
				model.addAttribute("patient", p);
				return "patient_edit"; // return p model in patient_edit form
			} else{
				model.addAttribute("message", "Patient not found");
				model.addAttribute("patient", p);
				return "index";
			}
		} catch (SQLException e){
			model.addAttribute("message", "SQL Error. " +e.getMessage());
			model.addAttribute("patient", p);
			return "index";
		}
	}


	/*
	 * Process changes from patient_edit form
	 *  Primary doctor, street, city, state, zip can be changed
	 *  ssn, patient id, name, birthdate, ssn are read only in template.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView p, Model model) throws SQLException {


		try (Connection con = getConnection();){
			PreparedStatement ps = con.prepareStatement("update patient set street=?, city=?,state=?, zipcode=?, primaryName=?");

			PreparedStatement ps2 = con.prepareStatement("select id from doctor where last_name =?");// VALIDATE DOC FROM LAST NAME
			ps2.setString(1, p.getPrimaryName());
			ResultSet rs = ps2.executeQuery();
			if (rs.next()){
				int docID = rs.getInt(1); // doctor confirmed if ID is found
			}
			else {
				model.addAttribute("message", "Doctor not found."); // error message if no doctor ID found
				model.addAttribute("patient", p);
				return "patient_edit"; // returns to edit
			}// End of validation


			ps.setString(1,p.getStreet());
			ps.setString(2, p.getCity());
			ps.setString(3,p.getState());
			ps.setString(4,p.getZipcode());
			ps.setString(5, p.getPrimaryName());

			//int rc = ps.executeUpdate();
			int rc;

			if((rc = ps.executeUpdate()) ==3){
				model.addAttribute("message", "Update Successful");
				model.addAttribute("patient", p);
				return "patient_show";
			} else{
				model.addAttribute("message", "Error. Update was not successful");
				System.out.println(rc);
				model.addAttribute("patient", p);
				return "patient_edit";
			}
		}catch (SQLException e){
			model.addAttribute("message", "SQL Error. " + e.getMessage());
			model.addAttribute("patient", p);
			return "doctor_edit";
		}
	}

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}


}
