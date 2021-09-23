package logic;

import common.EMFactory;
import common.TomcatStartUp;
import common.ValidationException;
import entity.BloodBank;
import entity.BloodDonation;
import entity.BloodGroup;
import entity.RhesusFactor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.persistence.EntityManager;
import static logic.BloodDonationLogic.ID;
import static logic.BloodDonationLogic.BANK_ID;
import static logic.BloodDonationLogic.BLOOD_GROUP;
import static logic.BloodDonationLogic.CREATED;
import static logic.BloodDonationLogic.MILLILITERS;
import static logic.BloodDonationLogic.RHESUS_FACTOR;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This class is has the example of how to add dependency when working with junit. it is commented because some
 * components need to be made first. You will have to import everything you need.
 *@reference Shariar (Shawn) Emami
 * @update by Simon Ao
 */
class BloodDonationTest {

    private BloodDonationLogic logic;
    private BloodDonation expectedEntity;

    @BeforeAll
    final static void setUpBeforeClass() throws Exception {
        TomcatStartUp.createTomcat( "/SimpleBloodBank", "common.ServletListener", "simplebloodbank-PU-test" );
    }

    @AfterAll
    final static void tearDownAfterClass() throws Exception {
        TomcatStartUp.stopAndDestroyTomcat();
    }

    @BeforeEach
    final void setUp() throws Exception {

        logic = LogicFactory.getFor( "BloodDonation" );
        /* **********************************
         * ***********IMPORTANT**************
         * **********************************/
        //we only do this for the test.
        //always create Entity using logic.
        //we manually make the blood donation to not rely on any logic functionality , just for testing

        //get an instance of EntityManager
        EntityManager em = EMFactory.getEMF().createEntityManager();
        //start a Transaction
        em.getTransaction().begin();
        //check if the depdendecy exists on DB already
        //em.find takes two arguments, the class type of return result and the primery key.
        BloodBank bb = em.find( BloodBank.class, 1 );
        //if result is null create the entity and persist it
        if( bb == null ){
            //cearet object
            bb = new BloodBank();
            bb.setName( "JUNIT" );
            bb.setPrivatelyOwned( true );
            bb.setEstablished( logic.convertStringToDate( "1111-11-11 11:11:11" ) );
            bb.setEmplyeeCount( 111 );
            //persist the dependency first
            em.persist( bb );
        }

        //create the desired entity
        BloodDonation entity = new BloodDonation();
        entity.setMilliliters( 100 );
        entity.setBloodGroup( BloodGroup.AB );
        entity.setRhd( RhesusFactor.Negative );
        //Below format matches HTML <input type=“datetime-local”
        entity.setCreated( logic.convertStringToDateTime("2000-02-22T22:22" ) );
        //add dependency to the desired entity
        entity.setBloodBank( bb );

        //add desired entity to hibernate, entity is now managed.
        //we use merge instead of add so we can get the managed entity.
        expectedEntity = em.merge( entity );
        //commit the changes
        em.getTransaction().commit();
        //close EntityManager
        em.close();
    }

    @AfterEach
    final void tearDown() throws Exception {
        if( expectedEntity != null ){
            logic.delete( expectedEntity );
        }
        BloodBankLogic bbLogic = LogicFactory.getFor("BloodBank");
        BloodBank bb = bbLogic.getBloodBankWithName("BB");
        if(bb != null) {
            bbLogic.delete(bb);
        }
    }

    @Test
    final void testGetAll() {
        //get all the blood donation from the DB
        List<BloodDonation> list = logic.getAll();
        //store the size of list, this way we know how many bloodDonations exits in DB
        int originalSize = list.size();

        //make sure blood donation was created successfully
        assertNotNull( expectedEntity );
        //delete the new blood donation
        logic.delete( expectedEntity );

        //get all bloodDonations again
        list = logic.getAll();
        //the new size of bloodDonations must be one less
        assertEquals( originalSize - 1, list.size() );
    }
    
    /**
     * helper method for testing all blood donation fields
     *
     * @param expected
     * @param actual
    */
    private void assertBloodDonationEquals( BloodDonation expected, BloodDonation actual ) {
        //assert all field to guarantee they are the same
        assertEquals( expected.getId(), actual.getId() );
        assertEquals( expected.getMilliliters(), actual.getMilliliters() );
        assertEquals( expected.getBloodGroup(), actual.getBloodGroup() );
        assertEquals( expected.getRhd(), actual.getRhd() );
        assertEquals( expected.getCreated(), actual.getCreated() );
        assertEquals( expected.getBloodBank().getId(), actual.getBloodBank().getId() );
        
    }
    
    @Test
    final void testGetWithId() {
        //using the id of test blood donation get another blood donation from logic
        BloodDonation returnedBloodDonation = logic.getWithId( expectedEntity.getId() );

        //the two bloodDonations (testAcounts and returnedBloodDonations) must be the same
        assertBloodDonationEquals( expectedEntity, returnedBloodDonation );
    }
    
     @Test
    final void getBloodDonationWithMilliliters() {
        boolean foundFull = false;
        //using the Milliiters of test blood donation get another blood donation from logic
         List<BloodDonation> returnedBloodDonation = logic.getBloodDonationWithMilliliters( expectedEntity.getMilliliters());
        for(BloodDonation bloodDonation: returnedBloodDonation){
            assertEquals( expectedEntity.getMilliliters(), bloodDonation.getMilliliters() );
             if( bloodDonation.getId().equals( expectedEntity.getId() ) ){
                assertBloodDonationEquals( expectedEntity, bloodDonation );
                foundFull = true;
            }
        }
        assertTrue( foundFull, "if zero means not found" );
    }
    
     @Test
    final void getBloodDonationWithBloodGroup() {
        boolean foundFull = false;
        //using the bloodgroup test blood donation get another blood donation from logic
          List<BloodDonation> returnedBloodDonation = logic.getBloodDonationWithBloodGroup( expectedEntity.getBloodGroup());
        for(BloodDonation bloodDonation: returnedBloodDonation){
            assertEquals( expectedEntity.getBloodGroup(), bloodDonation.getBloodGroup() );
             if( bloodDonation.getId().equals( expectedEntity.getId() ) ){
                assertBloodDonationEquals( expectedEntity, bloodDonation );
                foundFull = true;
            }
        }
        assertTrue( foundFull, "if zero means not found" );
    }
     @Test
    final void getBloodDonationWithCreated() {
        boolean foundFull = false;
        //using the rhd of test blood donation get another blood donation from logic
         List<BloodDonation> returnedBloodDonation = logic.getBloodDonationWithCreated( expectedEntity.getCreated());
        for(BloodDonation bloodDonation: returnedBloodDonation){
            assertEquals( expectedEntity.getCreated(), bloodDonation.getCreated() );
             if( bloodDonation.getId().equals( expectedEntity.getId() ) ){
                assertBloodDonationEquals( expectedEntity, bloodDonation );
                foundFull = true;
            }
        }
        assertTrue( foundFull, "if zero means not found");
    }
     @Test
    final void getBloodDonationsWithRhd() {
        boolean foundFull = false;
        //using the rhd of test blood donation get another blood donation from logic
         List<BloodDonation> returnedBloodDonation = logic.getBloodDonationsWithRhd( expectedEntity.getRhd());
        for(BloodDonation bloodDonation: returnedBloodDonation){
            assertEquals( expectedEntity.getRhd(), bloodDonation.getRhd() );
             if( bloodDonation.getId().equals( expectedEntity.getId() ) ){
                assertBloodDonationEquals( expectedEntity, bloodDonation );
                foundFull = true;
            }
        }
        assertTrue( foundFull, "if zero means not found" );       
    }
    
    @Test
    final void getBloodDonationsWithBloodBank() {
        boolean foundFull = false;
        //using the rhd of test blood donation get another blood donation from logic
         List<BloodDonation> returnedBloodDonation = logic.getBloodDonationsWithBloodBank( expectedEntity.getBloodBank().getId());
        for(BloodDonation bloodDonation: returnedBloodDonation){
            assertEquals( expectedEntity.getBloodBank().getId(), bloodDonation.getBloodBank().getId() );
             if( bloodDonation.getId().equals( expectedEntity.getId() ) ){
                assertBloodDonationEquals( expectedEntity, bloodDonation );
                foundFull = true;
            }
        }
        assertTrue( foundFull, "if zero means not found" );      
    }
    
     @Test
    final void testGetColumnNames() {
        List<String> list = logic.getColumnNames();
        assertEquals( Arrays.asList("ID","Bank_id", "Created", "Rhesus_factor", "Blood_group", "Milliliters" ), list );
    }
    
    @Test
    final void testGetColumnCodes() {
        List<String> list = logic.getColumnCodes();
        assertEquals( Arrays.asList(ID, BANK_ID, CREATED, RHESUS_FACTOR, BLOOD_GROUP,MILLILITERS ), list );
    }
    
    @Test
    final void testExtractDataAsList() {
        List<?> list = logic.extractDataAsList( expectedEntity );
        assertEquals( expectedEntity.getId(), list.get( 0 ) );
        assertEquals( expectedEntity.getBloodBank().getId(), list.get( 1 ) );
        assertEquals( expectedEntity.getCreated(), list.get( 2 ) );
        assertEquals( expectedEntity.getRhd(), list.get( 3 ) );
        assertEquals( expectedEntity.getBloodGroup(), list.get( 4 ) );
        assertEquals( expectedEntity.getMilliliters(), list.get( 5 ) );
    } 
    
     @Test
    final void testCreateEntityAndAdd() {
        Map<String, String[]> sampleMap = new HashMap<>();   
        sampleMap.put( BloodDonationLogic.CREATED, new String[]{ "1999-09-22T22:22" } );
        sampleMap.put( BloodDonationLogic.RHESUS_FACTOR, new String[]{ "Positive" } );
        sampleMap.put( BloodDonationLogic.BLOOD_GROUP, new String[]{ "A" } );
        sampleMap.put( BloodDonationLogic.MILLILITERS, new String[]{ "1000" } );
        sampleMap.put( BloodDonationLogic.BANK_ID, new String[]{ "1" } );
        

        BloodDonation returnedBloodDonation = logic.createEntity( sampleMap );
        logic.add( returnedBloodDonation );

        returnedBloodDonation = logic.getWithId( returnedBloodDonation.getId() );

        assertEquals( sampleMap.get( BloodDonationLogic.CREATED )[ 0 ], logic.convertDateTimeToString(returnedBloodDonation.getCreated()) );
        assertEquals( sampleMap.get( BloodDonationLogic.RHESUS_FACTOR )[ 0 ], returnedBloodDonation.getRhd().name() );
        assertEquals( sampleMap.get(  BloodDonationLogic.BLOOD_GROUP )[ 0 ], returnedBloodDonation.getBloodGroup().name() );
        assertEquals( sampleMap.get(  BloodDonationLogic.MILLILITERS )[ 0 ], Integer.toString(returnedBloodDonation.getMilliliters()) );
        assertEquals( sampleMap.get(  BloodDonationLogic.BANK_ID )[ 0 ], Integer.toString(returnedBloodDonation.getBloodBank().getId()) );
        logic.delete( returnedBloodDonation );
    } 
     @Test
    final void testCreateEntity() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( BloodDonationLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
        sampleMap.put( BloodDonationLogic.CREATED, new String[]{logic.convertDateTimeToString(expectedEntity.getCreated()) } );
        sampleMap.put( BloodDonationLogic.MILLILITERS, new String[]{ Integer.toString(expectedEntity.getMilliliters()) } );
        sampleMap.put( BloodDonationLogic.RHESUS_FACTOR, new String[]{ expectedEntity.getRhd().name() } );
        sampleMap.put( BloodDonationLogic.BLOOD_GROUP, new String[] { expectedEntity.getBloodGroup().name() } );
        sampleMap.put( BloodDonationLogic.BANK_ID, new String[]{ Integer.toString(expectedEntity.getBloodBank().getId()) } );
        
        BloodDonation returnedBloodDonation = logic.createEntity( sampleMap );

        assertBloodDonationEquals( expectedEntity, returnedBloodDonation );
    }   
    
    @Test
    final void testCreateEntityNullAndEmptyValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( BloodDonationLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( BloodDonationLogic.MILLILITERS, new String[]{ Integer.toString(expectedEntity.getMilliliters()) } );
            map.put( BloodDonationLogic.CREATED, new String[]{ logic.convertDateTimeToString(expectedEntity.getCreated()) } );
            map.put( BloodDonationLogic.BLOOD_GROUP, new String[]{ expectedEntity.getBloodGroup().name() } );
            map.put( BloodDonationLogic.RHESUS_FACTOR, new String[]{ expectedEntity.getRhd().name() } );
            map.put( BloodDonationLogic.BANK_ID, new String[]{ Integer.toString(expectedEntity.getBloodBank().getId()) } );
        };

        //idealy every test should be in its own method
        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.CREATED, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace(BloodDonationLogic.CREATED, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace(BloodDonationLogic.MILLILITERS, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.MILLILITERS, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.BLOOD_GROUP, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.BLOOD_GROUP, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.RHESUS_FACTOR, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.RHESUS_FACTOR, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.BANK_ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.BANK_ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
    }
    
    @Test
    final void testCreateEntityBadLengthValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( BloodDonationLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( BloodDonationLogic.MILLILITERS, new String[]{ Integer.toString(expectedEntity.getMilliliters()) } );
            map.put( BloodDonationLogic.CREATED, new String[]{ logic.convertDateTimeToString(expectedEntity.getCreated()) } );
            map.put( BloodDonationLogic.BLOOD_GROUP, new String[]{ expectedEntity.getBloodGroup().name() } );
            map.put( BloodDonationLogic.RHESUS_FACTOR, new String[]{ expectedEntity.getRhd().name() } );
            map.put( BloodDonationLogic.BANK_ID, new String[]{ Integer.toString(expectedEntity.getBloodBank().getId()) } );
        };

        IntFunction<String> generateString = ( int length ) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            //from 97 inclusive to 123 exclusive
            return new Random().ints( 'a', 'z' + 1 ).limit( length )
                    .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                    .toString();
        };

        //idealy every test should be in its own method
        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.ID, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.ID, new String[]{ "12b" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.CREATED, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.CREATED, new String[]{ generateString.apply( 10 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.MILLILITERS, new String[]{ "" } );
        assertThrows( NumberFormatException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.MILLILITERS, new String[]{ "12b" } );
        assertThrows( NumberFormatException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.BLOOD_GROUP, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.BLOOD_GROUP, new String[]{ generateString.apply( 3 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.RHESUS_FACTOR, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.RHESUS_FACTOR, new String[]{ generateString.apply( 9 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        
         fillMap.accept( sampleMap );
        sampleMap.replace( BloodDonationLogic.BANK_ID, new String[]{ "" } );
        assertThrows( NumberFormatException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( BloodDonationLogic.BANK_ID, new String[]{ "a" } );
        assertThrows( NumberFormatException.class, () -> logic.createEntity( sampleMap ) );
    }
    
    @Test
    final void testCreateEntityEdgeValues() {
        IntFunction<String> generateString = ( int length ) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            return new Random().ints( 'a', 'z' + 1 ).limit( length )
                    .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                    .toString();
        };

        Map<String, String[]> sampleMap = new HashMap<>();
       
        sampleMap.put( PersonLogic.ID, new String[]{ "1" } );
        sampleMap.put( BloodDonationLogic.CREATED, new String[]{ "1999-09-22T22:22" } );
        sampleMap.put( BloodDonationLogic.MILLILITERS, new String[]{ "0" } );
        sampleMap.put( BloodDonationLogic.RHESUS_FACTOR, new String[]{ "Negative" } );
        sampleMap.put( BloodDonationLogic.BLOOD_GROUP, new String[]{ "O" } );
        sampleMap.put( BloodDonationLogic.BANK_ID, new String[]{ Integer.toString(expectedEntity.getBloodBank().getId()) } ); 

        //idealy every test should be in its own method
        BloodDonation returnedBloodDonation = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( BloodDonationLogic.ID )[ 0 ] ), returnedBloodDonation.getId() );
        assertEquals( sampleMap.get( BloodDonationLogic.CREATED )[ 0 ], logic.convertDateTimeToString(returnedBloodDonation.getCreated()));
        assertEquals( sampleMap.get( BloodDonationLogic.MILLILITERS )[ 0 ], Integer.toString(returnedBloodDonation.getMilliliters()));
        assertEquals( sampleMap.get( BloodDonationLogic.BLOOD_GROUP )[ 0 ], returnedBloodDonation.getBloodGroup().name());
        assertEquals( sampleMap.get( BloodDonationLogic.RHESUS_FACTOR )[ 0 ], returnedBloodDonation.getRhd().name());
        assertEquals( sampleMap.get( BloodDonationLogic.BANK_ID )[ 0 ], Integer.toString(returnedBloodDonation.getBloodBank().getId()));
         

        sampleMap = new HashMap<>();
        sampleMap.put( BloodDonationLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( BloodDonationLogic.CREATED, new String[]{ "1999-09-22T22:22" } );
        sampleMap.put( BloodDonationLogic.MILLILITERS, new String[]{ Integer.toString(Integer.MAX_VALUE) } );
        sampleMap.put( BloodDonationLogic.RHESUS_FACTOR, new String[]{ "Positive" } );
        sampleMap.put( BloodDonationLogic.BLOOD_GROUP, new String[]{ "AB" } );
        sampleMap.put( BloodDonationLogic.BANK_ID, new String[]{ Integer.toString(expectedEntity.getBloodBank().getId()) } );

        //idealy every test should be in its own method
        returnedBloodDonation = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( BloodBankLogic.ID )[ 0 ] ), returnedBloodDonation.getId() );
        assertEquals( sampleMap.get( BloodDonationLogic.CREATED )[ 0 ], logic.convertDateTimeToString(returnedBloodDonation.getCreated()));
        assertEquals( sampleMap.get( BloodDonationLogic.MILLILITERS )[ 0 ], Integer.toString(returnedBloodDonation.getMilliliters()));
        assertEquals( sampleMap.get(BloodDonationLogic.BLOOD_GROUP )[ 0 ], returnedBloodDonation.getBloodGroup().name());
        assertEquals( sampleMap.get( BloodDonationLogic.RHESUS_FACTOR )[ 0 ], returnedBloodDonation.getRhd().name());
        assertEquals( sampleMap.get( BloodDonationLogic.BANK_ID )[ 0 ], Integer.toString(returnedBloodDonation.getBloodBank().getId()));
    }    
  
}
