package view;

import entity.BloodBank;
import entity.BloodDonation;
import entity.BloodGroup;
import entity.DonationRecord;
import entity.Person;
import entity.RhesusFactor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.BloodBankLogic;
import logic.BloodDonationLogic;
import logic.DonationRecordLogic;
import logic.Logic;
import logic.PersonLogic;
import logic.LogicFactory;

/**
 *
 * @author Rong Fu
 */
@WebServlet( name = "DonateBloodFrom", urlPatterns = { "/DonateBloodFrom" } )
public class DonateBloodFrom extends HttpServlet {

    private String errorMessage = null;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     *
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        response.setContentType( "text/html;charset=UTF-8" );
        try( PrintWriter out = response.getWriter() ) {
            /* TODO output your page here. You may use following sample code. */
            out.println( "<!DOCTYPE html>" );
            out.println( "<html>" );
            out.println( "<head>" );
            out.println( "<title>Donate Blood From</title>" );
            out.println( "</head>" );
            out.println( "<body>" );
            out.println( "<div style=\"text-align: center;\">" );
            out.println( "<div style=\"display: inline-block; text-align: left;\">" );
            out.println( "<form method=\"post\">" );
            out.println( "<h3>Person</h3><table >" );
            out.println( "<tr><td>First Name</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td>", PersonLogic.FIRST_NAME );
            out.println( "<td>Last Name</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td></tr>", PersonLogic.LAST_NAME );
            out.println( "<tr><td>Phone</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td>", PersonLogic.PHONE );
            out.println( "<td>Address</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td></tr>", PersonLogic.ADDRESS );
            out.println( "<tr><td>DoB</td>" );
            out.printf( "<td><input type=\"datetime-local\" name=\"%s\" value=\"\" size=30></td></tr>", PersonLogic.BIRTH );
            out.println( "</table><br/>" );
            
            out.println( "<h3>Blood</h3><table >" );
            out.println( "<tr><td>Blood Group</td>" );
            out.printf( "<td><Select name=\"%s\" value=\"\">", BloodDonationLogic.BLOOD_GROUP );
            out.printf( "<Option>A</Option><Option>B</Option><Option>AB</Option><Option>O</Option></Select></td>");
            out.println( "<td>RHD</td>" );
            out.printf( "<td><Select name=\"%s\" value=\"\">", BloodDonationLogic.RHESUS_FACTOR );
            out.printf( "<Option>Positive</Option><Option>Negative</Option></Select></td></tr>");
            out.println( "<tr><td>Amount</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td>", BloodDonationLogic.MILLILITERS );
            out.println( "<td>Tested</td>" );
            out.printf( "<td><Select name=\"%s\" value=\"\">", DonationRecordLogic.TESTED );
            out.printf( "<Option value=\"false\">Negative</Option><Option value=\"true\">Positive</Option></Select></td></tr>");
            out.println( "</table><br/>" );
            
            out.println( "<h3>Administration</h3><table >" );
            out.println( "<tr><td>Hospital</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td>", DonationRecordLogic.HOSPITAL );
            out.println( "<td>Administrator</td>" );
            out.printf( "<td><input type=\"text\" name=\"%s\" value=\"\" size=30></td></tr>", DonationRecordLogic.ADMINISTRATOR );
            out.println( "<tr><td>Date</td>" );
            out.printf( "<td><input type=\"datetime-local\" name=\"%s\" value=\"\" size=30></td>", DonationRecordLogic.CREATED );
            out.println( "<td>BloodBank</td>" );
            out.printf( "<td><Select name=\"%s\" value=\"\">", BloodDonationLogic.BANK_ID );
            Logic<BloodBank> bloodBankLogic = LogicFactory.getFor( "BloodBank" );
            bloodBankLogic.getAll().forEach( c -> out.printf( "<Option value=%s>%s</Option>", c.getId(), c.getName()) );
            out.println( "</Select></td></tr>" );
//            out.printf( "<Option>BloodBank</Option><Option>Bank</Option></td></tr>");
            out.println( "</table><br/>" );
            
            out.printf( "<input type=\"hidden\" name=\"%s\">", DonationRecordLogic.PERSON_ID);
            out.printf( "<input type=\"hidden\" name=\"%s\">", DonationRecordLogic.DONATION_ID);
            
//            out.println( "<table><tr><td/><td/><td/><td/></tr>" );
            out.println( "<input type=\"submit\" name=\"add\" value=\"Add\" style=\"text-align: right;\">" );
            out.println( "</form>" );
            if( errorMessage != null && !errorMessage.isEmpty() ){
                out.println( "<p color=red>" );
                out.println( "<font color=red size=4px>" );
                out.println( errorMessage );
                out.println( "</font>" );
                out.println( "</p>" );
                //erase errorMessage, or it will still be displayed after correcting input
                errorMessage = null;
            }
            out.println( "<pre>" );
            out.println( "Submitted keys and values:" );
            out.println( toStringMap( request.getParameterMap() ) );
            out.println( "</pre>" );
            out.println( "</div>" );
            out.println( "</div>" );
            out.println( "</body>" );
            out.println( "</html>" );
        }
    }

    private String toStringMap( Map<String, String[]> values ) {
        StringBuilder builder = new StringBuilder();
        values.forEach( ( k, v ) -> builder.append( "Key=" ).append( k )
                .append( ", " )
                .append( "Value/s=" ).append( Arrays.toString( v ) )
                .append( System.lineSeparator() ) );
        return builder.toString();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * get method is called first when requesting a URL. since this servlet will create a host this method simple
     * delivers the html code. creation will be done in doPost method.
     *
     * @param request servlet request
     * @param response servlet response
     *
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        log( "GET" );
        processRequest( request, response );
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * this method will handle the creation of entity. as it is called by user submitting data through browser.
     *
     * @param request servlet request
     * @param response servlet response
     *
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        log( "POST" );
        PersonLogic pLogic = LogicFactory.getFor( "Person" );
        BloodDonationLogic bdLogic = LogicFactory.getFor( "BloodDonation" );
        DonationRecordLogic drLogic = LogicFactory.getFor( "DonationRecord" );
        BloodBankLogic bLogic = LogicFactory.getFor( "BloodBank" );
        try {
            Person person = pLogic.createEntity( request.getParameterMap() );
            pLogic.add( person );
//            log("New personId:"+person.getId());

            BloodBank bBank = bLogic.getWithId(Integer.parseInt(request.getParameterMap().get(BloodDonationLogic.BANK_ID)[0]));
//            log("bBank:"+bBank.getId());
            BloodDonation bloodDonation = bdLogic.createEntity( request.getParameterMap() );
            bloodDonation.setBloodBank(bBank);
            bdLogic.add( bloodDonation );
//            log("New bloodDonation:"+bloodDonation.getId());
            
//            log("New bloodDonationId:"+bloodDonation.getId());
            DonationRecord donationRecord = drLogic.createEntity(request.getParameterMap());
            donationRecord.setPerson(person);
            donationRecord.setBloodDonation(bloodDonation);
            drLogic.add(donationRecord);
//            log("New donation record:"+donationRecord.getId());
            
        } catch( Exception ex ) {
//            log("Exception", ex);
            errorMessage = ex.getMessage();
        }
        
        if( request.getParameter( "add" ) != null ){
            //if add button is pressed return the same page
            processRequest( request, response );
        } else if( request.getParameter( "view" ) != null ){
            //if view button is pressed redirect to the appropriate table
            response.sendRedirect( "PersonTable" );
        }
    }
    

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Create a Person Entity";
    }

    private static final boolean DEBUG = true;

    public void log( String msg ) {
        if( DEBUG ){
            String message = String.format( "[%s] %s", getClass().getSimpleName(), msg );
            getServletContext().log( message );
        }
    }

    public void log( String msg, Throwable t ) {
        String message = String.format( "[%s] %s", getClass().getSimpleName(), msg );
        getServletContext().log( message, t );
    }
}

