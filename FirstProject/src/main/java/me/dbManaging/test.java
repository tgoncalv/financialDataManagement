package me.dbManaging;

import java.util.ArrayList;
import java.util.Collection;

import fr.moneyManaging.MoneyTypes;

public class test {

	public static void main(String[] args) {
		Double aa=0.123456;
		aa = MoneyTypes.round(aa,2);
		System.out.println(aa);
		Collection<String> test = new ArrayList<String>();
		System.out.println(test.size());
		test.add("cc");
		test.add("ddefs");
		String a = String.join(", ", test);
		System.out.println(a);
//		/*DbConnection.initializeDb("taiga");
//		List<String> profileList = new ArrayList<String>();
//		profileList = Profile.showProfileList();
//		for (String profileName : profileList) {
//			System.out.println(profileName);
//		}
//		
//		MoneyTypes.createMoney("taiga", "heuro");
//		MoneyTypes.deleteMoney("taiga", "heuro");
//		HashMap<Integer, String> moneyTypes = new HashMap<Integer, String>();
//		moneyTypes = MoneyTypes.viewMoneyTypes("taiga");
//		System.out.println(moneyTypes);
//		*/
//	    LocalDate dt = new LocalDate("2004-10-03");
//	    LocalDate dt2 = dt.dayOfMonth().withMinimumValue().minusDays(1);
//	    System.out.println(dt.toString());
//	    
//	    
//	    /*
//	    MoneyTypes.createMoney("taiga","heuroaaaa");
//	    DbConnection.createAccount("taiga", "test10aa0", 2, 1, "re");
//		*/
//	    
//	    
//	    CurrentAccount testObject = new CurrentAccount("taiga", "test10aa0");
//	    //testObject.spend(85, "pour tester la méthode");
//	    //testObject.obtain(-483, "pour a");
//	    //testObject.deleteTransact(1);
//	    
//	    Multimap<Integer, Object> map = ArrayListMultimap.create();
//
//	    map = testObject.listTransact(testObject.getPointerDate());
//	    System.out.println(map);
//	    
//	    map = testObject.listEcoTransact();
//	    System.out.println(map);
//	    
//	    testObject.updateMonth();
//	    testObject.modifyConfSave(50);
//	    System.out.println(testObject.getTotalProfit());
//	    System.out.println(testObject.getAmountToSave());
//	    System.out.println(testObject.getTotalAdvancedSaving());
//	    
//	    
//	    //DbConnection.deleteAccount("taiga", 1);
	    
	}
}