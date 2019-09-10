package session22;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class PayrollMain {
	
	private static SessionFactory factory;
	
	public static void main(String[] args) {
		factory = new Configuration().configure().buildSessionFactory();
		
		Salary[] sals = new Salary[3];
		sals[0] = new Salary(Double.valueOf(1000), Double.valueOf(10), Double.valueOf(10), Double.valueOf(10));
		sals[1] = new Salary(Double.valueOf(2000), Double.valueOf(15), Double.valueOf(5), Double.valueOf(20));
		sals[2] = new Salary(Double.valueOf(3000), Double.valueOf(15), Double.valueOf(15), Double.valueOf(15));
		
		Employee[] emps = new Employee[6];
		Account[] accs = new Account[6];
		for(int i = 0; i<6; i++) {
			emps[i] = new Employee("Adrian" + (i+1), "Adrian" + (i+1) + "@adrian.com" , 12345678 + i, sals[i%3],
					accs[i] = new Account("Savings", "AdrianBank" , 12345+i, "ADRIANBANK00" + i, emps[i]), 
					Double.valueOf(500*i), true);
		}
		
		Session session = factory.openSession();
		
		
		for (Salary sal : sals) {
			System.out.println("Inserted Salary ID: " + addSalary(session, sal));
		}
		
		for (Employee emp : emps) {
			System.out.println("Inserted Employee ID: " + addEmployee(session, emp));
		}
		
		System.out.println("\n\n");
		
		List<Employee> empList = fetchEmployee(session);
		for (Employee employee : empList) {
			if (employee.getEmployeeId().equals(2)) continue;
			calculateSalary(employee, Date.valueOf("2019-09-30"), "paid");
			updateEmployee(session, employee);
		}

		System.out.println("\n\n");
		Employee emp = fetchEmployee(session, 3);
		emp.setOverpay(emp.getOverpay() + 150);
		updateEmployee(session, emp);		
		
		System.out.println("\n\n");
		empList = fetchEmployee(session);
		for (Employee employee : empList) {
			calculateSalary(employee, Date.valueOf("2019-10-31"), "paid");
			updateEmployee(session, employee);
		}

		System.out.println("\n\n");
		emp = fetchEmployee(session, 1);
		emp.setIsActive(false);
		Set<SalaryForMonth> salsFM = emp.getSalaryForMonth();
		SalaryForMonth sfm = null;
		for (SalaryForMonth salaryForMonth : salsFM) {
			if (salaryForMonth.getMonth().equals(Date.valueOf("2019-10-31"))){
				salaryForMonth.setPaymentStatus("cancelled");
				break;
			}	
		}
		sfm = emp.getSalaryForMonth(Date.valueOf("2019-10-21"), "paid", 21, 31);
		emp.getSalaryForMonth().add(sfm);
		updateEmployee(session, emp);
		
		System.out.println("\n\n");

		List<Salary> salList = fetchSalary(session);
		for (Salary sal: salList) {
			sal.setBaseSalary(1.1 * sal.getBaseSalary());
			updateSalary(session, sal);
		}
		
		System.out.println("\n\n");

		empList = fetchEmployee(session);
		for (Employee employee : empList) {
			calculateSalary(employee, Date.valueOf("2019-11-30"), "paid");
			updateEmployee(session, employee);
		}
		
		System.out.println("\n\n");

		emp = fetchEmployee(session, 4);
		Account acc = emp.getAccount();
		acc.setAccountNumber(987654);
		acc.setBankName("NewBank");
		acc.setBranchCode("NEWBANK01");
		acc.setType("Checking");
		updateEmployee(session, emp);
		
		emp = fetchEmployee(session, 6);
		acc = emp.getAccount();
		acc.setAccountNumber(456789);
		acc.setBankName("NewBank");
		acc.setBranchCode("NEWBANK01");
		acc.setType("Checking");
		updateEmployee(session, emp);
		
		System.out.println("\n\n");

		empList = fetchEmployee(session);
		for (Employee employee : empList) {
			calculateSalary(employee, Date.valueOf("2019-12-31"), "paid");
			updateEmployee(session, employee);
		}
		
		System.out.println("\n\n");

		
		List<SalaryForMonth> salfm = fetchSalaryForMonth(session, Date.valueOf("2019-10-01"), Date.valueOf("2019-12-31"));
		for (SalaryForMonth salaryForMonth : salfm) {
			System.out.println(salaryForMonth);
		}
		
		session.close();
	}
	

	public static Integer addSalary(Session session,Salary s){
		return  (Integer)session.save(s);
	}
	
	public static Salary fetchSalary(Session session, Integer id) {
		return session.get(Salary.class, id);
	}
	
	public static void updateSalary(Session session, Salary s) {
		Transaction tx = session.beginTransaction();
	
		try{
			
			session.update(s);
			session.flush();
			tx.commit();
		}catch(Exception e){
		
			tx.rollback();
			e.printStackTrace();
		}finally{
			try {
				tx.rollback();
			}catch(Exception e) {
				
			}
		}
	}
	
	public static List<Salary> fetchSalary(Session session){
		return session.createQuery("From Salary").list();
	}
	
	public static Integer addEmployee(Session session,Employee e){
		return  (Integer)session.save(e);
	}
	
	public static Employee fetchEmployee(Session session, Integer id) {
		return session.get(Employee.class, id);
	}
	
	public static List<Employee> fetchEmployee(Session session){
		return session.createQuery("From Employee").list();
	}
	
	public static void updateEmployee(Session session, Employee e) {
		Transaction tx = session.beginTransaction();
	
		try{
			
			session.update(e);
			session.flush();
			tx.commit();
		}catch(Exception err){
		
			tx.rollback();
			err.printStackTrace();
		}finally{
			try {
				tx.rollback();
			}catch(Exception err) {
				
			}
		}
	}
	
	public static void calculateSalary(Employee emp, Date date, String status) {
		if (emp.getIsActive()) {
			emp.getSalaryForMonth().add(emp.getSalaryForMonth(date, status));
		}
	}
	
	public static void calculateSalary(Employee emp, Date date, String status, Integer days, Integer daysThisMonth) {
		if (emp.getIsActive()) {
			emp.getSalaryForMonth().add(emp.getSalaryForMonth(date, status, days, daysThisMonth));
		}
	}
	
	public static Integer addSalaryForMonth(Session session, SalaryForMonth s) {
		return (Integer) session.save(s);
	}
	
	public static List<SalaryForMonth> fetchSalaryForMonth(Session session) {
		return session.createQuery("From SalaryForMonth").list();
	}
	
	public static List<SalaryForMonth> fetchSalaryForMonth(Session session, Date start, Date end) {
		return session.createQuery("From SalaryForMonth WHERE month BETWEEN '" + start.toString() + "' AND '" + end.toString() + "'").list();
	}
	

}