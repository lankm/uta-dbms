package tests;

import java.io.*;
import java.util.*;
import java.lang.*;

import heap.*;
import bufmgr.*;
import diskmgr.*;
import global.*;
import btree.*;

/**
 * Note that in JAVA, methods can't be overridden to be more private. Therefore,
 * the declaration of all private functions are now declared protected as
 * opposed to the private type in C++.
 */

/*
 *         CSE 4331/5331 B+ Tree Project (Spring 2024)
 *         Instructor: Abhishek Santra
 *
 */


class BTDriver implements GlobalConst {

	public BTreeFile file;
	public int postfix = 0;
	public int keyType;
	public BTFileScan scan;
	public int flag = 0;

	protected String dbpath;
	protected String logpath;
	public int deleteFashion;

	public void runTests() {
		Random random = new Random();
		dbpath = "BTREE" + random.nextInt() + ".minibase-db";
		logpath = "BTREE" + random.nextInt() + ".minibase-log";

		SystemDefs sysdef = new SystemDefs(dbpath, 5000, 5000, "Clock");
		System.out.println("\n" + "Running " + " tests...." + "\n");

		keyType = AttrType.attrInteger;

		// Kill anything that might be hanging around
		String newdbpath;
		String newlogpath;
		String remove_logcmd;
		String remove_dbcmd;
		String remove_cmd = "/bin/rm -rf ";

		newdbpath = dbpath;
		newlogpath = logpath;

		remove_logcmd = remove_cmd + logpath;
		remove_dbcmd = remove_cmd + dbpath;

		// Commands here is very machine dependent. We assume
		// user are on UNIX system here
		try {
			Runtime.getRuntime().exec(remove_logcmd);
			Runtime.getRuntime().exec(remove_dbcmd);
		} catch (IOException e) {
			System.err.println("IO error: " + e);
		}

		remove_logcmd = remove_cmd + newlogpath;
		remove_dbcmd = remove_cmd + newdbpath;

		// This step seems redundant for me. But it's in the original
		// C++ code. So I am keeping it as of now, just in case I
		// I missed something
		try {
			Runtime.getRuntime().exec(remove_logcmd);
			Runtime.getRuntime().exec(remove_dbcmd);
		} catch (IOException e) {
			System.err.println("IO error: " + e);
		}

		// Run the tests. Return type different from C++
		runAllTests();

		// Clean up again
		try {
			Runtime.getRuntime().exec(remove_logcmd);
			Runtime.getRuntime().exec(remove_dbcmd);
		} catch (IOException e) {
			System.err.println("IO error: " + e);
		}

		System.out.print("\n" + "..." + " Finished ");
		System.out.println(".\n\n");

	}

	private void menu() {
		System.out
				.println("-------------------------- MENU ------------------");

		System.out.println("\n[0]   Print the B+ Tree Structure");
		System.out.println("[1]   Print All Leaf Pages");
		System.out.println("[2]   Choose a Page (Index/Leaf) to Print");

		System.out
				.println("\n           --- Positive Integer Key (for choices [3]-[5]) ---");
		System.out.println("\n[3]   Insert a Record");
		System.out.println("[4]   Delete a Record (Naive Delete)");
		System.out.println("[5]   Delete some records (Naive Delete)");

		System.out.println("\n[6]  Quit!");
		System.out.print("Hi, make your choice :");
	}

	protected void runAllTests() {
		PageId pageno = new PageId();
		int key, n, m, num, choice, lowkeyInt, hikeyInt;
		RID rid;
		choice = 1;
		deleteFashion = 0; // naive delete
		try {
			System.out.println(" ***************** The file name is: " + "AAA"
					+ postfix + "  **********");
			file = new BTreeFile("AAA" + postfix, keyType, 4, 0);// naive delete
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		postfix = 0;
		while (choice != 6) {
			menu();

			try {
				choice = GetStuff.getChoice();

				switch (choice) {
				case 0:
					BT.printBTree(file.getHeaderPage());
					break;
				case 1:
					BT.printAllLeafPages(file.getHeaderPage());
					break;
				case 2:
					System.out.println("Input the page number: ");
					num = GetStuff.getChoice();
					if (num < 0)
						break;
					BT.printPage(new PageId(num), keyType);
					break;
				case 3:
					int i = 0,
					sum = 300;
                // ASantra [1/7/2023]: Removed arr[]]
					// int arr[] = new int[10000];
					keyType = AttrType.attrInteger;
					if (flag == 0) {
						while (i < 100) {
						//	key = (arr[i] = sum += 1);
                        	sum += 1;
                            key = sum;
						/*	if (key <= 0)
								break; */
							pageno.pid = key;
							rid = new RID(pageno, key);
							file.insert(new IntegerKey(key), rid);
							i++;
						}
						
						System.out
								.println("Initially 100 values have been inserted from 301 to 400.");
						// System.out.println("The inserted values are : "+
						// Arrays.toString(arr));
						flag = 1;
					} else {
						System.out
								.println("\n \n -----------------------------");
						System.out.println("1. Insert Single Value");
						System.out.println("2. Insert Multiple Values");
						System.out.println("3. Insert Values from a File");
						System.out.println("Make your choice(4 to exit) :");
						num = GetStuff.getChoice();
						if (num < 0)
							break;
						switch (num) {
						case 1:
							keyType = AttrType.attrInteger;
							System.out
									.println("Input the integer key to insert: ");
							key = GetStuff.getChoice();
							
                            if (key <= 0)
								break;
							
                            pageno.pid = key;
							rid = new RID(pageno, key);
							file.insert(new IntegerKey(key), rid);
							break;

						case 2:
							keyType = AttrType.attrInteger;
							System.out
									.println("Input the LOWER integer key (>0): ");
							lowkeyInt = GetStuff.getChoice();
							System.out
									.println("Input the HIGHER integer key (>0): ");
							hikeyInt = GetStuff.getChoice();
							if (hikeyInt <= 0 || lowkeyInt <= 0)
								break;
							for (key = lowkeyInt; key <= hikeyInt; key++) {
								pageno.pid = key;
								rid = new RID(pageno, key);
								file.insert(new IntegerKey(key), rid);
							}
							break;
						case 3:
							Scanner scanner = new Scanner(new File("test-insert-file.txt"));
						        while(scanner.hasNextInt()){
   								key = scanner.nextInt();
								if (key <= 0)
									break;
								pageno.pid = key;
								rid = new RID(pageno, key);
								file.insert(new IntegerKey(key), rid);
							}

						default:
							break;
						}
					}
					break;
				case 4:
					keyType = AttrType.attrInteger;
					System.out
							.println("Input the integer key to delete: ");
					key = GetStuff.getChoice();
					if (key <= 0)
						break;
					pageno.pid = key;
					rid = new RID(pageno, key);
					file.Delete(new IntegerKey(key), rid);
					break;
				case 5:
					keyType = AttrType.attrInteger;
					System.out
							.println("Input the LOWER integer key (>0): ");
					lowkeyInt = GetStuff.getChoice();
					System.out
							.println("Input the HIGHER integer key (>0): ");
					hikeyInt = GetStuff.getChoice();
					if (hikeyInt <= 0 || lowkeyInt <= 0)
						break;
					for (key = lowkeyInt; key <= hikeyInt; key++) {
						pageno.pid = key;
						rid = new RID(pageno, key);
						file.Delete(new IntegerKey(key), rid);
					}
					break;

				case 6:
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out
						.println("       !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out
						.println("       !!         Something is wrong                    !!");
				System.out
						.println("       !!     Is your DB full? then exit. rerun it!     !!");
				System.out
						.println("       !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			}
		}
	}

	void test1(int n) throws Exception {
		try {
			System.out.println(" ***************** The file name is: " + "AAA"
					+ postfix + "  **********");
			file = new BTreeFile("AAA" + postfix, keyType, 4, deleteFashion);
			file.traceFilename("TRACE");

			KeyClass key;
			RID rid = new RID();
			PageId pageno = new PageId();
			for (int i = 0; i < n; i++) {
				key = new IntegerKey(i);
				pageno.pid = i;
				rid = new RID(pageno, i);

				file.insert(key, rid);

			}

		} catch (Exception e) {
			throw e;
		}

	}

	void test2(int n) throws Exception {
		try {

			System.out.println(" ***************** The file name is: " + "AAA"
					+ postfix + "  **********");
			file = new BTreeFile("AAA" + postfix, keyType, 4, deleteFashion);
			file.traceFilename("TRACE");

			KeyClass key;
			RID rid = new RID();
			PageId pageno = new PageId();
			for (int i = 0; i < n; i++) {
				key = new IntegerKey(n - i);
				pageno.pid = n - i;
				rid = new RID(pageno, n - i);

				file.insert(key, rid);

			}

		} catch (Exception e) {
			throw e;
		}
	}

	void test3(int n) throws Exception {
		try {
			System.out.println(" ***************** The file name is: " + "AAA"
					+ postfix + "  **********");
			file = new BTreeFile("AAA" + postfix, keyType, 4, deleteFashion);
			file.traceFilename("TRACE");

			int[] k = new int[n];
			for (int i = 0; i < n; i++) {
				k[i] = i;
			}
			Random ran = new Random();
			int random;
			int tmp;
			for (int i = 0; i < n; i++) {

				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}

			KeyClass key;
			RID rid = new RID();
			PageId pageno = new PageId();
			for (int i = 0; i < n; i++) {
				key = new IntegerKey(k[i]);
				pageno.pid = k[i];
				rid = new RID(pageno, k[i]);

				file.insert(key, rid);

			}

		} catch (Exception e) {
			throw e;
		}
	}

	void test4(int n, int m) throws Exception {
		try {
			System.out.println(" ***************** The file name is: " + "AAA"
					+ postfix + "  **********");
			file = new BTreeFile("AAA" + postfix, keyType, 4, deleteFashion);
			file.traceFilename("TRACE");

			int[] k = new int[n];
			for (int i = 0; i < n; i++) {
				k[i] = i;
			}
			Random ran = new Random();
			int random;
			int tmp;
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}

			KeyClass key;
			RID rid = new RID();
			PageId pageno = new PageId();
			for (int i = 0; i < n; i++) {
				key = new IntegerKey(k[i]);
				pageno.pid = k[i];
				rid = new RID(pageno, k[i]);

				file.insert(key, rid);

			}

			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}

			for (int i = 0; i < m; i++) {
				key = new IntegerKey(k[i]);
				pageno.pid = k[i];
				rid = new RID(pageno, k[i]);

				if (file.Delete(key, rid) == false) {
					System.out
							.println("*********************************************************");
					System.out
							.println("*     Your delete method has bug!!!                     *");
					System.out
							.println("*     You insert a record, But you failed to delete it. *");
					System.out
							.println("*********************************************************");
				}
			}

		} catch (Exception e) {
			throw e;
		}
	}

	void test5(int n, int m) throws Exception {
		try {

			System.out.println(" ***************** The file name is: " + "AAA"
					+ postfix + "  **********");
			file = new BTreeFile("AAA" + postfix, keyType, 20, deleteFashion);
			file.traceFilename("TRACE");

			int[] k = new int[n];
			for (int i = 0; i < n; i++) {
				k[i] = i;
			}

			Random ran = new Random();
			int random;
			int tmp;
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}

			KeyClass key;
			RID rid = new RID();
			PageId pageno = new PageId();
			for (int i = 0; i < n; i++) {
				key = new StringKey("**" + k[i]);
				pageno.pid = k[i];
				rid = new RID(pageno, k[i]);

				file.insert(key, rid);

			}

			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}
			for (int i = 0; i < n; i++) {
				random = (ran.nextInt()) % n;
				if (random < 0)
					random = -random;
				tmp = k[i];
				k[i] = k[random];
				k[random] = tmp;
			}

			for (int i = 0; i < m; i++) {
				key = new StringKey("**" + k[i]);
				pageno.pid = k[i];
				rid = new RID(pageno, k[i]);

				if (file.Delete(key, rid) == false) {
					System.out
							.println("*********************************************************");
					System.out
							.println("*     Your delete method has bug!!!                     *");
					System.out
							.println("*     You insert a record, But you failed to delete it. *");
					System.out
							.println("*********************************************************");
				}

			}

		} catch (Exception e) {
			throw e;
		}
	}

}

/**
 * To get the integer off the command line
 */
class GetStuff {
	GetStuff() {
	}

	public static int getChoice() {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		int choice = -1;

		try {
			choice = Integer.parseInt(in.readLine());
		} catch (NumberFormatException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		}

		return choice;
	}

	public static void getReturn() {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {
			String ret = in.readLine();
		} catch (IOException e) {
		}
	}
}

public class BTTest implements GlobalConst {

	public static void main(String[] argvs) {

		try {
			BTDriver bttest = new BTDriver();
			bttest.runTests();
		} catch (Exception e) {
			e.printStackTrace();
			System.err
					.println("Error encountered during buffer manager tests:\n");
			Runtime.getRuntime().exit(1);
		}
	}

}
