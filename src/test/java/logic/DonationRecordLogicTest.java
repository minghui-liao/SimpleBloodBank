package logic;

import common.EMFactory;
import common.TomcatStartUp;
import common.ValidationException;
import entity.Account;
import entity.BloodBank;
import entity.BloodDonation;
import entity.BloodGroup;
import entity.DonationRecord;
import entity.Person;
import entity.RhesusFactor;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Minghui Liao
 */
public class DonationRecordLogicTest {
    
    private DonationRecordLogic logic;
    private DonationRecord expectedEntity;

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

        logic = LogicFactory.getFor( "DonationRecord" );
        /* **********************************
         * ***********IMPORTANT**************
         * **********************************/
        //we only do this for the test.
        //always create Entity using logic.
        //we manually make the account to not rely on any logic functionality , just for testing

        //get an instance of EntityManager
        EntityManager em = EMFactory.getEMF().createEntityManager();
        //start a Transaction
        em.getTransaction().begin();
        //check if the depdendecy exists on DB already
        //em.find takes two arguments, the class type of return result and the primery key.
        BloodDonation bd = em.find( BloodDonation.class, 1 );
        BloodBank bb = em.find(BloodBank.class, 1);
//        DonationRecord dr = em.find(DonationRecord.class, 1);
//        Set<DonationRecord> drSet = new HashSet<>();
//        drSet.add(dr);
        //if result is null create the entity and persist it
        if( bd == null ){
            //cearet object
            bd = new BloodDonation();
            bd.setMilliliters( 100 );
            bd.setBloodGroup( BloodGroup.AB );
            bd.setRhd( RhesusFactor.Negative );
            bd.setCreated( logic.convertStringToDateTime( "1111-11-11T11:11" ) );
//            bd.setBloodBank(bb );
            //persist the dependency first
            em.persist( bd );
        }
        
        //em.find takes two arguments, the class type of return result and the primery key.
        Person p = em.find( Person.class, 1 );
        //if result is null create the entity and persist it
        if( p == null ){
            //cearet object
            p = new Person();
            p.setFirstName("JUnit");
            p.setLastName("Test");
            p.setAddress("address");
            p.setPhone("123456");
            p.setBirth(logic.convertStringToDateTime("2020-02-02T11:11"));
//            p.setBloodBank(bb);
//            p.setDonationRecordSet(drSet);

            //persist the dependency first
            em.persist( p );
        }

        //create the desired entity
        DonationRecord entity = new DonationRecord();
        entity.setAdministrator("JUnit");
        entity.setHospital("Test");
        entity.setTested(true);
        entity.setCreated( logic.convertStringToDateTime( "2011-11-11T11:11" ) );
        //add dependency to the desired entity
        entity.setPerson(p);
        entity.setBloodDonation(bd);

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
        
        PersonLogic pLogic = LogicFactory.getFor("Person");
//        Person p = pLogic.getWithId(expectedEntity.getPerson().getId());
        if (expectedEntity.getPerson() != null) {
            pLogic.delete(expectedEntity.getPerson());  
        }       
//        if (p != null) pLogic.delete(p);
        
        BloodDonationLogic bdLogic = LogicFactory.getFor("BloodDonation");
//        BloodDonation bd = bdLogic.getWithId(expectedEntity.getBloodDonation().getId());
        if (expectedEntity.getBloodDonation() != null) {
            bdLogic.delete(expectedEntity.getBloodDonation());  
        }
//        if ( bd != null) bdLogic.delete(bd);
        
    }
    
    /**
     * helper method for testing all account fields
     *
     * @param expected
     * @param actual
     */
    private void assertDonationRecordEquals( DonationRecord expected, DonationRecord actual ) {
        //assert all field to guarantee they are the same
        assertEquals( expected.getId(), actual.getId() );
        assertEquals( expected.getAdministrator(), actual.getAdministrator() );
        assertEquals( expected.getTested(), actual.getTested() );
        assertEquals( expected.getCreated(), actual.getCreated() );
        assertEquals( expected.getBloodDonation().getId(), actual.getBloodDonation().getId() );
        assertEquals( expected.getPerson().getId(), actual.getPerson().getId() );
    }

    @Test
    final void testGetAll() {
        //get all the accounts from the DB
        List<DonationRecord> list = logic.getAll();
        //store the size of list, this way we know how many accounts exits in DB
        int originalSize = list.size();

        //make sure account was created successfully
        assertNotNull( expectedEntity );
        //delete the new account
        logic.delete( expectedEntity );

        //get all accounts again
        list = logic.getAll();
        //the new size of accounts must be one less
        assertEquals( originalSize - 1, list.size() );
    }
    
    @Test
    final void testGetWithId() {
        //using the id of test account get another account from logic
        DonationRecord returnedDonationRecord = logic.getWithId( expectedEntity.getId() );

        //the two accounts (testAcounts and returnedAccounts) must be the same
        assertDonationRecordEquals( expectedEntity, returnedDonationRecord );
    }
    
    @Test
    final void testGetDonationRecordWithTested() {
        int foundFull = 0;
        List<DonationRecord> returnedDonationRecords = logic.getDonationRecordWithTested(expectedEntity.getTested() );
        for( DonationRecord donationRecord: returnedDonationRecords ) {
            //all accounts must have the same TESTED
            assertEquals( expectedEntity.getTested(), donationRecord.getTested() );
            //exactly one account must be the same
            if( donationRecord.getId().equals( expectedEntity.getId() ) ){
                assertDonationRecordEquals( expectedEntity, donationRecord );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
    @Test
    final void testGetDonationRecordWithAdministrator() {
        int foundFull = 0;
        List<DonationRecord> returnedDonationRecords = logic.getDonationRecordWithAdministrator(expectedEntity.getAdministrator() );
        for( DonationRecord donationRecord: returnedDonationRecords ) {
            //all accounts must have the same TESTED
            assertEquals( expectedEntity.getAdministrator(), donationRecord.getAdministrator() );
            //exactly one account must be the same
            if( donationRecord.getId().equals( expectedEntity.getId() ) ){
                assertDonationRecordEquals( expectedEntity, donationRecord );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
    @Test
    final void testGetDonationRecordWithHospital() {
        int foundFull = 0;
        List<DonationRecord> returnedDonationRecords = logic.getDonationRecordWithHospital(expectedEntity.getHospital() );
        for( DonationRecord donationRecord: returnedDonationRecords ) {
            //all accounts must have the same TESTED
            assertEquals( expectedEntity.getHospital(), donationRecord.getHospital() );
            //exactly one account must be the same
            if( donationRecord.getId().equals( expectedEntity.getId() ) ){
                assertDonationRecordEquals( expectedEntity, donationRecord );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
    @Test
    final void testGetDonationRecordWithCreated() {
        int foundFull = 0;
        List<DonationRecord> returnedDonationRecords = logic.getDonationRecordWithCreated(expectedEntity.getCreated() );
        for( DonationRecord donationRecord: returnedDonationRecords ) {
            //all accounts must have the same TESTED
            assertEquals( expectedEntity.getCreated(), donationRecord.getCreated() );
            //exactly one account must be the same
            if( donationRecord.getId().equals( expectedEntity.getId() ) ){
                assertDonationRecordEquals( expectedEntity, donationRecord );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
    @Test
    final void testGetDonationRecordWithPerson() {
        int foundFull = 0;
        List<DonationRecord> returnedDonationRecords = logic.getDonationRecordWithPerson(expectedEntity.getPerson().getId() );
        for( DonationRecord donationRecord: returnedDonationRecords ) {
            //all accounts must have the same TESTED
            assertEquals( expectedEntity.getPerson().getId(), donationRecord.getPerson().getId() );
            //exactly one account must be the same
            if( donationRecord.getId().equals( expectedEntity.getId() ) ){
                assertDonationRecordEquals( expectedEntity, donationRecord );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
    @Test
    final void testGetDonationRecordWithDonation() {
        int foundFull = 0;
        List<DonationRecord> returnedDonationRecords = logic.getDonationRecordWithDonation(expectedEntity.getBloodDonation().getId() );
        for( DonationRecord donationRecord: returnedDonationRecords ) {
            //all DonationRecords must have the same TESTED
            assertEquals( expectedEntity.getBloodDonation().getId(), donationRecord.getBloodDonation().getId() );
            //exactly one DonationRecord must be the same
            if( donationRecord.getId().equals( expectedEntity.getId() ) ){
                assertDonationRecordEquals( expectedEntity, donationRecord );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
    @Test
    final void testCreateEntityAndAdd() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( DonationRecordLogic.PERSON_ID, new String[]{ Integer.toString(expectedEntity.getPerson().getId()) } );
        sampleMap.put( DonationRecordLogic.DONATION_ID, new String[]{ Integer.toString(expectedEntity.getBloodDonation().getId()) } );
        sampleMap.put( DonationRecordLogic.TESTED, new String[]{ "true" } );
        sampleMap.put( DonationRecordLogic.ADMINISTRATOR, new String[]{ "administratorTest" } );
        sampleMap.put( DonationRecordLogic.HOSPITAL, new String[]{ "hospitalTest" } );
        sampleMap.put( DonationRecordLogic.CREATED, new String[]{ "2001-11-11T11:11" } );

        DonationRecord returnedDonationRecord = logic.createEntity( sampleMap );
        logic.add( returnedDonationRecord );

        returnedDonationRecord = logic.getWithId( returnedDonationRecord.getId() );

        assertEquals( Integer.parseInt(sampleMap.get( DonationRecordLogic.PERSON_ID )[ 0 ]), returnedDonationRecord.getPerson().getId() );
        assertEquals( Integer.parseInt(sampleMap.get( DonationRecordLogic.DONATION_ID )[ 0 ]), returnedDonationRecord.getBloodDonation().getId() );
        assertEquals( Boolean.parseBoolean(sampleMap.get( DonationRecordLogic.TESTED )[ 0 ]), returnedDonationRecord.getTested() );
        assertEquals( sampleMap.get( DonationRecordLogic.ADMINISTRATOR )[ 0 ], returnedDonationRecord.getAdministrator() );
        assertEquals( sampleMap.get( DonationRecordLogic.HOSPITAL )[ 0 ], returnedDonationRecord.getHospital() );
        assertEquals( sampleMap.get( DonationRecordLogic.CREATED )[ 0 ], new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(returnedDonationRecord.getCreated()) );

        logic.delete( returnedDonationRecord );
    }

    @Test
    final void testCreateEntity() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( DonationRecordLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
        sampleMap.put( DonationRecordLogic.PERSON_ID, new String[]{ Integer.toString(expectedEntity.getPerson().getId()) } );
        sampleMap.put( DonationRecordLogic.DONATION_ID, new String[]{ Integer.toString(expectedEntity.getBloodDonation().getId()) } );
        sampleMap.put( DonationRecordLogic.TESTED, new String[]{ Boolean.toString(expectedEntity.getTested()) } );
        sampleMap.put( DonationRecordLogic.ADMINISTRATOR, new String[]{ expectedEntity.getAdministrator() } );
        sampleMap.put( DonationRecordLogic.HOSPITAL, new String[]{ expectedEntity.getHospital() } );
        sampleMap.put( DonationRecordLogic.CREATED, new String[]{ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(expectedEntity.getCreated()) } );

        DonationRecord returnedDonationRecord = logic.createEntity( sampleMap );

        assertDonationRecordEquals( expectedEntity, returnedDonationRecord );
    }

    @Test
    final void testCreateEntityNullAndEmptyValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( DonationRecordLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( DonationRecordLogic.PERSON_ID, new String[]{ Integer.toString(expectedEntity.getPerson().getId()) } );
            map.put( DonationRecordLogic.DONATION_ID, new String[]{ Integer.toString(expectedEntity.getBloodDonation().getId()) } );
            map.put( DonationRecordLogic.TESTED, new String[]{ Boolean.toString(expectedEntity.getTested()) } );
            map.put( DonationRecordLogic.ADMINISTRATOR, new String[]{ expectedEntity.getAdministrator() } );
            map.put( DonationRecordLogic.HOSPITAL, new String[]{ expectedEntity.getHospital() } );
            map.put( DonationRecordLogic.CREATED, new String[]{ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(expectedEntity.getCreated()) } );
        };

        //idealy every test should be in its own method
        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.ADMINISTRATOR, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.ADMINISTRATOR, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        //can be null
        sampleMap.replace( DonationRecordLogic.PERSON_ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.DONATION_ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.DONATION_ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.TESTED, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.TESTED, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.HOSPITAL, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.HOSPITAL, new String[]{});
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.CREATED, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.CREATED, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
    }

    @Test
    final void testCreateEntityBadLengthValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( DonationRecordLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( DonationRecordLogic.PERSON_ID, new String[]{ Integer.toString(expectedEntity.getPerson().getId()) } );
            map.put( DonationRecordLogic.DONATION_ID, new String[]{ Integer.toString(expectedEntity.getBloodDonation().getId()) } );
            map.put( DonationRecordLogic.TESTED, new String[]{ Boolean.toString(expectedEntity.getTested()) } );
            map.put( DonationRecordLogic.ADMINISTRATOR, new String[]{ expectedEntity.getAdministrator() } );
            map.put( DonationRecordLogic.HOSPITAL, new String[]{ expectedEntity.getHospital() } );
            map.put( DonationRecordLogic.CREATED, new String[]{ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(expectedEntity.getCreated()) } );
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
        sampleMap.replace( DonationRecordLogic.ID, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.ID, new String[]{ "12b" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

//        fillMap.accept( sampleMap );
//        sampleMap.replace( DonationRecordLogic.PERSON_ID, new String[]{ "" } );
//        assertNotNull( logic.createEntity( sampleMap ) );
//        sampleMap.replace( DonationRecordLogic.PERSON_ID, new String[]{ generateString.apply( 46 ) } );
//        assertNotNull( logic.createEntity( sampleMap ) );
//
//        fillMap.accept( sampleMap );
//        sampleMap.replace( DonationRecordLogic.DONATION_ID, new String[]{ "" } );
//        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
//        sampleMap.replace( DonationRecordLogic.DONATION_ID, new String[]{ generateString.apply( 46 ) } );
//        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
 
        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.TESTED, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.TESTED, new String[]{ generateString.apply( 46 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.ADMINISTRATOR, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.ADMINISTRATOR, new String[]{ generateString.apply( 101 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.HOSPITAL, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( DonationRecordLogic.HOSPITAL, new String[]{ generateString.apply( 101 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( DonationRecordLogic.CREATED, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
    }

    @Test
    final void testCreateEntityEdgeValues() {
        IntFunction<String> generateString = ( int length ) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            return new Random().ints( 'a', 'z' + 1 ).limit( length )
                    .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                    .toString();
        };
        
        IntFunction<String> generateBoolean = ( Void ) -> {
            Random random = new Random();
            if (random.nextInt(1) == 1) {
                return "true";
            } else {
                return "false";
            }
        };


        IntFunction<String> generateDate = ( Void ) -> {
            Date now = new Date();
            long timestamp = now.getTime();
            Date randomDate = new Date(ThreadLocalRandom.current().nextLong(timestamp));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            return simpleDateFormat.format(randomDate);
        };

        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( DonationRecordLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( DonationRecordLogic.PERSON_ID, new String[]{ expectedEntity.getPerson().getId().toString() } );
        sampleMap.put( DonationRecordLogic.DONATION_ID, new String[]{ expectedEntity.getBloodDonation().getId().toString() } );
        sampleMap.put( DonationRecordLogic.TESTED, new String[]{ generateBoolean.apply( 0 ) } );
        sampleMap.put( DonationRecordLogic.ADMINISTRATOR, new String[]{ generateString.apply( 1 ) } );
        sampleMap.put( DonationRecordLogic.HOSPITAL, new String[]{ generateString.apply( 45 ) } );
        sampleMap.put( DonationRecordLogic.CREATED, new String[]{ generateDate.apply(0) } );

        //idealy every test should be in its own method
        DonationRecord returnedDonationRecord = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt(sampleMap.get( DonationRecordLogic.ID )[ 0 ] ), returnedDonationRecord.getId() );
        assertEquals( Integer.parseInt(sampleMap.get( DonationRecordLogic.PERSON_ID )[ 0 ]), returnedDonationRecord.getPerson().getId() );
        assertEquals( Integer.parseInt(sampleMap.get( DonationRecordLogic.DONATION_ID )[ 0 ]), returnedDonationRecord.getBloodDonation().getId() );
        assertEquals( Boolean.parseBoolean(sampleMap.get( DonationRecordLogic.TESTED )[ 0 ]), returnedDonationRecord.getTested() );
        assertEquals( sampleMap.get( DonationRecordLogic.ADMINISTRATOR )[ 0 ], returnedDonationRecord.getAdministrator() );
        assertEquals( sampleMap.get( DonationRecordLogic.HOSPITAL )[ 0 ], returnedDonationRecord.getHospital() );
        assertEquals( sampleMap.get( DonationRecordLogic.CREATED )[ 0 ], new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(returnedDonationRecord.getCreated()) );

        sampleMap = new HashMap<>();
        sampleMap.put( DonationRecordLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( DonationRecordLogic.PERSON_ID, new String[]{ expectedEntity.getPerson().getId().toString() } );
        sampleMap.put( DonationRecordLogic.DONATION_ID, new String[]{ expectedEntity.getBloodDonation().getId().toString() } );
        sampleMap.put( DonationRecordLogic.TESTED, new String[]{ generateBoolean.apply( 0 ) } );
        sampleMap.put( DonationRecordLogic.ADMINISTRATOR, new String[]{ generateString.apply( 45 ) } );
        sampleMap.put( DonationRecordLogic.HOSPITAL, new String[]{ generateString.apply( 45 ) } );
        sampleMap.put( DonationRecordLogic.CREATED, new String[]{ generateDate.apply( 0 ) } );

        //idealy every test should be in its own method
        returnedDonationRecord = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( DonationRecordLogic.ID )[ 0 ] ), returnedDonationRecord.getId() );
        assertEquals( Integer.parseInt( sampleMap.get( DonationRecordLogic.PERSON_ID )[ 0 ]), returnedDonationRecord.getPerson().getId() );
        assertEquals( Integer.parseInt( sampleMap.get( DonationRecordLogic.DONATION_ID )[ 0 ]), returnedDonationRecord.getBloodDonation().getId() );
        assertEquals( Boolean.parseBoolean(sampleMap.get( DonationRecordLogic.TESTED )[ 0 ]), returnedDonationRecord.getTested() );
        assertEquals( sampleMap.get( DonationRecordLogic.ADMINISTRATOR )[ 0 ], returnedDonationRecord.getAdministrator() );
        assertEquals( sampleMap.get( DonationRecordLogic.HOSPITAL )[ 0 ], returnedDonationRecord.getHospital() );
        assertEquals( sampleMap.get( DonationRecordLogic.CREATED )[ 0 ], new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(returnedDonationRecord.getCreated()) );
    }

    @Test
    final void testGetColumnNames() {
        List<String> list = logic.getColumnNames();
        assertEquals( Arrays.asList( "ID", "Person_id", "Donation_id", "Tested", "Administrator", "Hospital", "Created" ), list );
    }

    @Test
    final void testGetColumnCodes() {
        List<String> list = logic.getColumnCodes();
        assertEquals( Arrays.asList( DonationRecordLogic.ID, DonationRecordLogic.PERSON_ID, DonationRecordLogic.DONATION_ID, DonationRecordLogic.TESTED, DonationRecordLogic.ADMINISTRATOR, DonationRecordLogic.HOSPITAL, DonationRecordLogic.CREATED ), list );
    }

    @Test
    final void testExtractDataAsList() {
        List<?> list = logic.extractDataAsList( expectedEntity );
        assertEquals( expectedEntity.getId(), list.get( 0 ) );
        assertEquals( expectedEntity.getPerson().getId(), list.get( 1 ) );
        assertEquals( expectedEntity.getBloodDonation().getId(), list.get( 2 ) );
        assertEquals( expectedEntity.getTested(), list.get( 3 ) );
        assertEquals( expectedEntity.getAdministrator(), list.get( 4 ) );
        assertEquals( expectedEntity.getHospital(), list.get( 5 ) );
        assertEquals( expectedEntity.getCreated(), list.get( 6 ) );
    }
}
