package view;

import entity.BloodBank;
import entity.Person;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.AccountLogic;
import logic.BloodBankLogic;
import logic.Logic;
import logic.LogicFactory;

/**
 *
 * @author Hui Lyu
 */

@WebServlet(name="CreateBloodBank", urlPatterns="/CreateBloodBank")
public class CreateBloodBank extends HttpServlet{
    
    private String errorMessage = null;
    
    protected void processRequest( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        response.setContentType( "text/html;charset=UTF-8" );
        try( PrintWriter out = response.getWriter() ) {
            /* TODO output your page here. You may use following sample code. */
            out.println( "<!DOCTYPE html>" );
            out.println( "<html>" );
            out.println( "<head>" );
            out.println( "<title>Create Blood Bank</title>" );
            out.println( "</head>" );
            out.println( "<body>" );
            out.println( "<div style=\"text-align: center;\">" );
            out.println( "<div style=\"display: inline-block; text-align: left;\">" );
            out.println( "<form method=\"post\">" );
            out.println( "Owner:<br>" );
            out.printf( "<Select name=\"%s\" value=\"\">", BloodBankLogic.OWNER_ID);
            out.println( "<Option value=\"\">----</Option>" );
            Logic<Person> personLogic = LogicFactory.getFor( "Person" );
            personLogic.getAll().forEach( c -> out.printf( "<Option value=%s>%s</Option>", c.getId(), c.getFirstName()+" "+c.getLastName()) );
            out.println( "</Select><br>" );
            out.println( "<br>" );
            out.println( "Name:<br>" );
            out.printf( "<input type=\"text\" name=\"%s\" value=\"\"><br>", BloodBankLogic.NAME );
            out.println( "<br>" );
            out.println( "Privately_Owned:<br>" );
            out.printf( "<Select name=\"%s\" value=\"\">", BloodBankLogic.PRIVATELY_OWNED);
            out.println( "<Option>True</Option>" );
            out.println( "<Option>False</Option>" );
            out.println( "</Select><br>" );
            out.println( "<br>" );
            out.println( "Establish Date:<br>" );
            out.printf( "<input type=\"datetime-local\" name=\"%s\" value=\"\" size=30><br>", BloodBankLogic.ESTABLISHED);
           // out.printf( "<input type=\"date\" name=\"%s\" value=\"\"><br>", BloodBankLogic.ESTABLISHED );
            out.println( "<br>" );
             out.println( "Employee Count:<br>" );
            out.printf( "<input type=\"text\" name=\"%s\" value=\"\"><br>", BloodBankLogic.EMPLOYEE_COUNT);
            out.println( "<br>" );
            out.println( "<input type=\"submit\" name=\"view\" value=\"Add and View\">" );
            out.println( "<input type=\"submit\" name=\"add\" value=\"Add\">" );
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
        BloodBankLogic bloodBankLogic = LogicFactory.getFor( "BloodBank" );
        
        try{
            BloodBank bloodBank = bloodBankLogic.createEntity(request.getParameterMap());
            bloodBankLogic.add(bloodBank);
        }catch(Exception ex ){
            log("CreateBloodBank doPost", ex);
            errorMessage = ex.getMessage();
        }
        
//        String username = request.getParameter( AccountLogic.USERNAME );
//        if( bloodBankLogic.getAccountWithUsername( username ) == null ){
//            try {
//                Account account = aLogic.createEntity( request.getParameterMap() );
//                aLogic.add( account );
//            } catch( Exception ex ) {
//                errorMessage = ex.getMessage();
//            }
//        } else {
//            //if duplicate print the error message
//            errorMessage = "Username: \"" + username + "\" already exists";
//        }
        if( request.getParameter( "add" ) != null ){
            //if add button is pressed return the same page
            processRequest( request, response );
        } else if( request.getParameter( "view" ) != null ){
            //if view button is pressed redirect to the appropriate table
            response.sendRedirect( "BloodBankTable" );
        }
    }
    
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Create a Account Entity";
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
