package logic;

import common.ValidationException;
import dal.BloodDonationDAL;
import entity.BloodBank;
import entity.BloodDonation;
import entity.BloodGroup;
import entity.RhesusFactor;
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
import static logic.DonationRecordLogic.CREATED;

/**
 *
 * @author Simon Ao
 */
public class BloodDonationLogic extends GenericLogic<BloodDonation, BloodDonationDAL>  {
    
    /**
     * create static final variables with proper name of each column. this way you will never manually type it again,
     * instead always refer to these variables.
     *
     * by using the same name as column id and HTML element names we can make our code simpler. this is not recommended
     * for proper production project.
     */
    
    public static final String BLOOD_GROUP = "blood_group";
    public static final String MILLILITERS  = "milliliters";
    public static final String RHESUS_FACTOR  = "rhesus_factor";
    public static final String CREATED  = "created";
    public static final String ID  = "id";
    public static final String BANK_ID  = "bank_id";
    
             
   BloodDonationLogic(){
        super ( new BloodDonationDAL());
    }
   
    public List<BloodDonation> getBloodDonationWithMilliliters( int milliliters ) {
        return get( () -> dal().findByMilliliters( milliliters ) );
    }
    
     public List<BloodDonation> getBloodDonationWithBloodGroup( BloodGroup bloodGroup ) {
        return get( () -> dal().findByBloodGroup( bloodGroup ) );
    }
      public List<BloodDonation> getBloodDonationWithCreated( Date created ) {
        return get( () -> dal().findByCreated( created ) );
    }
       public List<BloodDonation> getBloodDonationsWithRhd( RhesusFactor rhd ) {
        return get( () -> dal().findByRhd( rhd ) );
    }
        public List<BloodDonation> getBloodDonationsWithBloodBank( int bloodBankId ) {
        return get( () -> dal().findByBloodBank( bloodBankId ) );
    }
       
        
     @Override
    public BloodDonation createEntity(Map<String, String[]> parameterMap) {
        Objects.requireNonNull( parameterMap, "parameterMap cannot be null" );
           BloodDonation entity = new BloodDonation();
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
        
              
        String rhesus_factor= parameterMap.get( RHESUS_FACTOR )[ 0 ];
        String bloodGroups = parameterMap.get( BLOOD_GROUP )[0];
        String milliliters = parameterMap.get( MILLILITERS )[ 0 ];
        String band_id = parameterMap.get( BANK_ID )[ 0 ];
        
        //validate the data
        validator.accept(rhesus_factor, 8);
        validator.accept(bloodGroups, 2);
//        validator.accept(milliliters, 45);
//        validator.accept(band_id, 45);
//        validator.accept(created,45);

         //set values on entity
        entity.setCreated( convertStringToDateTime(parameterMap.get( CREATED )[ 0 ]));
        entity.setMilliliters(Integer.valueOf(milliliters));
        entity.setBloodGroup(converStringToBloodBank(bloodGroups) );
        entity.setRhd( RhesusFactor.getRhesusFactor(rhesus_factor) );
        entity.setBloodBank(new BloodBank(Integer.valueOf(band_id))  );
        return entity;
        
    }
    
     private BloodGroup converStringToBloodBank(String s){
        switch(s){
            case "A":
                return BloodGroup.A;
            case "B":
                return BloodGroup.B;
            case "AB":
                return BloodGroup.AB;
            case "O":
                return BloodGroup.O;
        }
        return null;
    }   
    
   
    @Override
    public List<String> getColumnNames() {
        return Arrays.asList( "ID","Bank_id", "Created", "Rhesus_factor", "Blood_group", "Milliliters" );
    } 
   
   

    public BloodDonation updateEntity(Map<String, String[]> parameterMap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    @Override
    public List<String> getColumnCodes() {
        return Arrays.asList( ID, BANK_ID, CREATED, RHESUS_FACTOR, BLOOD_GROUP,MILLILITERS);
    }

    @Override
    public List<?> extractDataAsList(BloodDonation e) {
        return Arrays.asList( e.getId(), e.getBloodBank()!=null?e.getBloodBank().getId():"", e.getCreated(), e.getRhd(), e.getBloodGroup(), e.getMilliliters());
    }

      
    @Override
    public List<BloodDonation> getAll() {
       return get( () -> dal().findAll() );
    }

    @Override
    public BloodDonation getWithId(int id) {
         return get( () -> dal().findById( id ) );
    }
    
}
