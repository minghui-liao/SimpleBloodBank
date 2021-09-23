package dal;


import entity.BloodBank;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hui Lyu
 */
public class BloodBankDAL extends GenericDAL<BloodBank> {
   
    public BloodBankDAL(){
        super(BloodBank.class);
    }

    @Override
    public List<BloodBank> findAll() {
        return findResults("BloodBank.findAll",null);
    }

    @Override
    public BloodBank findById(int bankId) {
        Map<String,Object> map = new HashMap<>();
        map.put("bankId", bankId);
        
        return findResult("BloodBank.findByBankId",map);
    }
    /**
     * The method find the blood bank by name
     * @param name
     * @return BloodBank
     */
    public BloodBank findByName(String name){
        Map<String,Object> map = new HashMap<>();
        map.put("name", name);
        
        return findResult("BloodBank.findByName", map);
    }
    
    /**
     * The method find the private owned blood bank
     * @param privatelyOwned (true or false)
     * @return list of blood banks
     */
    public List<BloodBank> findByPrivatelyOwned(boolean privatelyOwned){
        Map<String, Object> map = new HashMap<>();
        map.put("privatelyOwned", privatelyOwned);
        
        return findResults("BloodBank.findByPrivatelyOwned", map);
    }
    
    /**
     * The method find the blood bank by established date
     * @param established date
     * @return list of blood banks
     */
    public List<BloodBank> findByEstablished(Date established){
        Map<String, Object> map = new HashMap<>();
        map.put("established",established);
        
        return findResults("BloodBank.findByEstablished", map);
    }
    
    /**
     * The method find the blood bank by the employee number
     * @param employeeCount
     * @return list of blood banks
     */
    public List<BloodBank> findByEmployeeCount(int employeeCount){
        Map<String, Object> map = new HashMap<>();
        map.put("emplyeeCount", employeeCount);
        
        return findResults("BloodBank.findByEmplyeeCount", map);
    }
    
    /**
     * The method find the blood bank by owner
     * @param ownerId
     * @return blood bank
     */
    public BloodBank findByOwner(int ownerId){
        Map<String, Object> map = new HashMap<>();
        map.put("ownerId", ownerId);
        
        return findResult("BloodBank.findByOwner", map);
    }
}
 
    

