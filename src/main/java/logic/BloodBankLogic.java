package logic;


import common.ValidationException;
import dal.BloodBankDAL;
import entity.BloodBank;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ObjIntConsumer;


/**
 *
 * @author Hui Lyu
 */
public class BloodBankLogic extends GenericLogic<BloodBank,BloodBankDAL>{

    public static final String OWNER_ID = "owner_id";
    public static final String PRIVATELY_OWNED = "privately_owned";
    public static final String ESTABLISHED = "established";
    public static final String NAME = "name";
    public static final String EMPLOYEE_COUNT = "employee_count";
    public static final String ID = "id";
    
    
    BloodBankLogic(){
        super (new BloodBankDAL());
    }
    
    
    @Override
    public List<BloodBank> getAll() {
        return get(()-> dal().findAll());
    }


    
    @Override
    public BloodBank getWithId(int id) {
       return get(() -> dal().findById(id));
    }
    
    public BloodBank getBloodBankWithName(String name){
        return get(() -> dal().findByName(name));
    }
    
    public List<BloodBank> getBloodBankWithPrivatelyOwned(boolean privatelyOwned){
        return get(()-> dal().findByPrivatelyOwned(privatelyOwned));
    }
    
    public List<BloodBank> getBloodBankWithEstablished(Date established){
        return get(() -> dal().findByEstablished(established));
    }
    
    public BloodBank getBloodBanksWithOwner(int ownerId){
        return get(() -> dal().findByOwner(ownerId));
    }
           
    
    public List<BloodBank> getBloodBanksWithEmployeeCount(int count){
        return get(() -> dal().findByEmployeeCount(count));
    }
    
    
    @Override
    public BloodBank createEntity(Map<String, String[]> parameterMap) {
       Objects.requireNonNull(parameterMap, "parameterMap cannot be null");
       
       BloodBank entity = new BloodBank();
       
       if(parameterMap.containsKey(ID)){
           try{
               entity.setId(Integer.parseInt(parameterMap.get(ID)[0]));
           } catch (java.lang.NumberFormatException ex){
               throw new ValidationException(ex);
           }
       }
       
       
       // error checking 
       ObjIntConsumer<String> validator = (value, length) -> {
           if (value == null || value.trim().isEmpty() || value.length()> length){
               String error = "";
               if (value == null || value.trim().isEmpty()){
                   error = "value cannot be null or empty: " + value;
               }
               if(value.length() > length){
                   error = "string length is " + value.length() + ">" + length;
               }
               throw new ValidationException( error );
           }
       };
       
       String owner_id = parameterMap.get(OWNER_ID)[0];
       String name = parameterMap.get(NAME)[0];
       String established = parameterMap.get(ESTABLISHED)[0];
       String employeeCount = parameterMap.get(EMPLOYEE_COUNT)[0];
       String privateOwned = parameterMap.get(PRIVATELY_OWNED)[0];
       
       
       //validate the data
       validator.accept(name, 100);   
       
       //set values on entity
       if(owner_id!=null && !owner_id.isEmpty()) { 
           int ownerId = Integer.parseInt(owner_id);
           if(getBloodBanksWithOwner(ownerId)!=null) throw new ValidationException( "The selected owner has already owns a blood bank and can't own more." );
           PersonLogic pLogic = LogicFactory.getFor( "Person" );
           entity.setOwner(pLogic.getWithId(ownerId) );
       }
       entity.setName(name);
       Boolean booleanPrivatelyOwned = Boolean.parseBoolean(privateOwned);
       entity.setPrivatelyOwned(booleanPrivatelyOwned);
       entity.setEstablished(convertStringToDateTime(established));
       entity.setEmplyeeCount(Integer.parseInt(employeeCount));   
       return entity;
    }


    
    @Override
    public List<String> getColumnNames() {
        return Arrays.asList("Bank_ID", "Owner", "Name","Privately Owned", "Established", "Employee Count");
               
    }

    @Override
    public List<String> getColumnCodes() {
       return Arrays.asList(ID,OWNER_ID,NAME,PRIVATELY_OWNED,ESTABLISHED,EMPLOYEE_COUNT);
    }


    @Override
    public List<?> extractDataAsList(BloodBank e) {
        return Arrays.asList(e.getId()+"",e.getOwner()!=null?e.getOwner().getId():"",e.getName(), e.getPrivatelyOwned(), convertDateToString(e.getEstablished()), e.getEmplyeeCount());
    }
    
}
