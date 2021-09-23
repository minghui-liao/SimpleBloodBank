package logic;

import common.ValidationException;
import dal.DonationRecordDAL;
import entity.BloodDonation;
import entity.DonationRecord;
import entity.Person;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.ObjIntConsumer;

/**
 *
 * @author Minghui Liao
 */

public class DonationRecordLogic extends GenericLogic<DonationRecord, DonationRecordDAL> {

    /**
     * create static final variables with proper name of each column. this way you will never manually type it again,
     * instead always refer to these variables.
     *
     * by using the same name as column id and HTML element names we can make our code simpler. this is not recommended
     * for proper production project.
     */
    public static final String PERSON_ID = "person_id";
    public static final String DONATION_ID = "donation_id";
    public static final String TESTED = "tested";
    public static final String ADMINISTRATOR = "administrator";
    public static final String HOSPITAL = "hospital";
    public static final String CREATED = "created";
    public static final String ID = "id";

    DonationRecordLogic() {
        super( new DonationRecordDAL() );
    }

    @Override
    public List<DonationRecord> getAll() {
        return get( () -> dal().findAll() );
    }

    @Override
    public DonationRecord getWithId( int id ) {
        return get( () -> dal().findById( id ) );
    }

    public List<DonationRecord> getDonationRecordWithTested( boolean tested ) {
        return get( () -> dal().findByTested( tested ) );
    }

    public List<DonationRecord> getDonationRecordWithAdministrator( String administrator ) {
        return get( () -> dal().findByAdministrator( administrator ) );
    }

    public List<DonationRecord> getDonationRecordWithHospital( String username ) {
        return get( () -> dal().findByHospital( username ) );
    }

    public List<DonationRecord> getDonationRecordWithCreated( Date created ) {
        return get( () -> dal().findByCreated( created ) );
    }

    public List<DonationRecord> getDonationRecordWithPerson( int personId ) {
        return get( () -> dal().findByPerson( personId ) );
    }
    
    public List<DonationRecord> getDonationRecordWithDonation( int donationId ) {
        return get( () -> dal().findByDonation( donationId ) );
    }
    
    @Override
    public DonationRecord createEntity( Map<String, String[]> parameterMap ) {
        //do not create any logic classes in this method.

//        return new DonationRecordBuilder().SetData( parameterMap ).build();
        Objects.requireNonNull( parameterMap, "parameterMap cannot be null" );
        //same as if condition below
//        if (parameterMap == null) {
//            throw new NullPointerException("parameterMap cannot be null");
//        }

        //create a new Entity object
        DonationRecord entity = new DonationRecord();

        //ID is generated, so if it exists add it to the entity object
        //otherwise it does not matter as mysql will create an if for it.
        //the only time that we will have id is for update behaviour.
        if( parameterMap.containsKey( ID ) ){
            try {
                entity.setId( Integer.parseInt( parameterMap.get( ID )[ 0 ] ) );
            } catch( java.lang.NumberFormatException ex ) {
                throw new ValidationException( ex );
            }
        }

        //before using the values in the map, make sure to do error checking.
        //simple lambda to validate a string, this can also be place in another
        //method to be shared amoung all logic classes.
        ObjIntConsumer< String> validator = ( value, length ) -> {
            if( value == null || value.trim().isEmpty() || value.length() > length ){
                String error = "";
                if( value == null || value.trim().isEmpty() ){
                    error = "value cannot be null or empty: " + value;
                }
                if( value.length() > length ){
                    error = "string length is " + value.length() + " > " + length;
                }
                throw new ValidationException( error );
            }
        };

        //extract the date from map first.
        //everything in the parameterMap is string so it must first be
        //converted to appropriate type. have in mind that values are
        //stored in an array of String; almost always the value is at
        //index zero unless you have used duplicated key/name somewhere.

        String person_id = parameterMap.get (PERSON_ID) [ 0 ];
        String donation_id = parameterMap.get( DONATION_ID )[ 0 ];
        String tested = parameterMap.get( TESTED )[ 0 ];
        String administrator = parameterMap.get( ADMINISTRATOR )[ 0 ];
        String hospital = parameterMap.get( HOSPITAL )[ 0 ];

        //validate the data
        validator.accept( administrator, 100 );
        validator.accept( hospital, 100 );  

        Boolean testedb = Boolean.parseBoolean(tested);
        if (!tested.equals(testedb.toString())) {
            throw new ValidationException( "tested is invalid" );
        } 

        //set values on entity
        if(person_id!=null && !person_id.isEmpty()) { 
            PersonLogic pLogic = LogicFactory.getFor( "Person" );
            entity.setPerson( pLogic.getWithId(Integer.parseInt(person_id)) );
            if(entity.getPerson()==null) throw new ValidationException( "Person_id is invalid" );
        }
        if(donation_id!=null && !donation_id.isEmpty()) {
            BloodDonationLogic bdLogic = LogicFactory.getFor( "BloodDonation" );
            entity.setBloodDonation( bdLogic.getWithId(Integer.parseInt(donation_id)) );
            if(entity.getBloodDonation()==null) throw new ValidationException( "Donation_id is invalid" );
        }

        entity.setTested( testedb );
        entity.setAdministrator( administrator );
        entity.setHospital( hospital );
        entity.setCreated( convertStringToDateTime(parameterMap.get( CREATED )[ 0 ]));

        return entity;
    }

    /**
     * this method is used to send a list of all names to be used form table column headers. by having all names in one
     * location there is less chance of mistakes.
     *
     * this list must be in the same order as getColumnCodes and extractDataAsList
     *
     * @return list of all column names to be displayed.
     */
    @Override
    public List<String> getColumnNames() {
        return Arrays.asList("ID", "Person_id", "Donation_id", "Tested", "Administrator", "Hospital", "Created" );
    }

    /**
     * this method returns a list of column names that match the official column names in the db. by having all names in
     * one location there is less chance of mistakes.
     *
     * this list must be in the same order as getColumnNames and extractDataAsList
     *
     * @return list of all column names in DB.
     */
    @Override
    public List<String> getColumnCodes() {
        return Arrays.asList( ID, PERSON_ID, DONATION_ID, TESTED, ADMINISTRATOR, HOSPITAL, CREATED );
    }

    /**
     * return the list of values of all columns (variables) in given entity.
     *
     * this list must be in the same order as getColumnNames and getColumnCodes
     *
     * @param e - given Entity to extract data from.
     *
     * @return list of extracted values
     */
    @Override
    public List<?> extractDataAsList( DonationRecord e ) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(e.getTested());
        System.out.println(e.getAdministrator());
        System.out.println(dateFormat.format(e.getCreated()));
        return Arrays.asList( e.getId(),e.getPerson()!=null?e.getPerson().getId():"", e.getBloodDonation()!=null?e.getBloodDonation().getId():"", e.getTested(), e.getAdministrator(), e.getHospital(), e.getCreated() );
    }
}

