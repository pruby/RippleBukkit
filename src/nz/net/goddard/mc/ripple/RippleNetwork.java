package nz.net.goddard.mc.ripple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RippleNetwork {
	private Map<String, RippleAccount> accounts;
	private Map<String, Currency> currencies;
	private Map<String, Set<Trust>> trustsToUser;
	private Map<String, Set<Debt>> debtsToUser;
	
	public static final int MAX_PATH_DEPTH = 10;
	
	public RippleNetwork() {
		this.accounts = new HashMap<String, RippleAccount>();
		this.currencies = new HashMap<String, Currency>();
		this.trustsToUser = new HashMap<String, Set<Trust>>();
		this.debtsToUser = new HashMap<String, Set<Debt>>();
	}
	
	private static RippleNetwork loadRippleNetwork(File inFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		RippleNetwork network = new RippleNetwork();
		ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(inFile)));

		// Type and version
		String header = input.readUTF();
		if (!header.equals("Ripple Network")) {
			throw new RuntimeException("Invalid Ripple Network File");
		}
		
		int version = input.readInt();
		if (version != 1) {
			throw new RuntimeException("Invalid Ripple Network File Version");
		}
		
		int accountCount = input.readInt();
		for (int i = 0; i < accountCount; ++i) {
			RippleAccount account = RippleAccount.load(network, input);
			network.addAccount(account);
		}
		
		int currencyCount = input.readInt();
		for (int i = 0; i < currencyCount; ++i) {
			Currency currency = Currency.load(input);
			network.addCurrency(currency);
		}
		
		int trustCount = input.readInt();
		for (int i = 0; i < trustCount; ++i) {
			Trust trust = Trust.load(network, input);
			network.addTrust(trust);
		}
		
		int debtCount = input.readInt();
		for (int i = 0; i < debtCount; ++i) {
			Debt debt = Debt.load(network, input);
			network.addDebt(debt);
		}
		
		return network;
	}
	
	private void addCurrency(Currency currency) {
		currencies.put(currency.getAbbreviation(), currency);
	}

	private void addAccount(RippleAccount account) {
		accounts.put(account.getAccountName(), account);
	}

	private synchronized void saveRippleNetwork(File outFile) throws FileNotFoundException, IOException {
		File tempFile = File.createTempFile("ripplenetwork", ".temp", outFile.getParentFile());
		
		ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(tempFile)));
		
		// Type and version
		output.writeUTF("Ripple Network");
		output.writeInt(1);
		
		output.writeInt(accounts.size());
		for (RippleAccount account : accounts.values()) {
			account.save(output);
		}
		
		output.writeInt(currencies.size());
		for (Currency currency : currencies.values()) {
			currency.save(output);
		}
		
		int trustCount = 0;
		for (Set<Trust> trusts : trustsToUser.values()) {
			trustCount += trusts.size();
		}
		
		output.writeInt(trustCount);
		for (Set<Trust> trusts : trustsToUser.values()) {
			for (Trust trust : trusts) {
				trust.save(output);
			}
		}
		
		int debtCount = 0;
		for (Set<Debt> debts : debtsToUser.values()) {
			debtCount += debts.size();
		}
		
		output.writeInt(debtCount);
		for (Set<Debt> debts : debtsToUser.values()) {
			for (Debt debt : debts) {
				debt.save(output);
			}
		}
		
		output.flush();
		output.close();
		
		tempFile.renameTo(outFile);
	}
	
	public Trust getTrust(String from, String to, Currency currency) {
		return getTrust(from, to, currency, false);
	}
	
	public boolean deleteTrust(String from, String to, Currency currency) {
		return getTrust(from, to, currency, true) != null;
	}
	
	public synchronized void addTrust(Trust trust) {
		Set<Trust> trusts = trustsToUser.get(trust.getTrustTo());
		if (trusts == null) {
			trusts = new TreeSet<Trust>();
			trustsToUser.put(trust.getTrustTo(), trusts);
		}
		trusts.add(trust);
	}
	
	public synchronized void addDebt(Debt debt) {
		Set<Debt> trusts = debtsToUser.get(debt.getOwedTo());
		if (trusts == null) {
			trusts = new TreeSet<Debt>();
			debtsToUser.put(debt.getOwedTo(), trusts);
		}
		trusts.add(debt);
	}
		
	private synchronized Trust getTrust(String from, String to, Currency currency, boolean delete) {
		Set<Trust> trusts = trustsToUser.get(to);
		if (trusts != null)  {
			Trust found = null;
			
			for (Trust trust : trusts) {
				if (trust.getTrustFrom().equals(from) && trust.getTrustTo().equals(to) && trust.getCurrency().equals(currency)) {
					found = trust;
					break;
				}
			}
			
			if (found != null) {
				if (delete) {
					trusts.remove(found);
				}
				return found;
			}
		}
		
		return null;
	}
	
	public Debt getDebt(String from, String to, Currency currency) {
		return getDebt(from, to, currency, false);
	}
	
	public boolean deleteDebt(String from, String to, Currency currency) {
		return getDebt(from, to, currency, true) != null;
	}
	
	public List<String> getDebtors(String to, Currency currency) {
		List<String> result = new LinkedList<String>();
		for (Debt debt : debtsToUser.get(to)) {
			if (debt.getCurrency().equals(currency)) {
				result.add(debt.getOwedBy());
			}
		}
		return result;
	}
	
	public List<String> getTrustors(String to, Currency currency) {
		List<String> result = new LinkedList<String>();
		for (Trust trust : trustsToUser.get(to)) {
			if (trust.getCurrency().equals(currency)) {
				result.add(trust.getTrustFrom());
			}
		}
		return result;
	}
	
	private synchronized Debt getDebt(String from, String to, Currency currency, boolean delete) {
		Set<Debt> debts = debtsToUser.get(to);
		if (debts != null)  {
			Debt found = null;
			
			for (Debt debt : debts) {
				if (debt.getOwedBy().equals(from) && debt.getOwedTo().equals(to) && debt.getCurrency().equals(currency)) {
					found = debt;
					break;
				}
			}
			
			if (found != null) {
				if (delete) {
					debts.remove(found);
				}
				return found;
			}
		}
		
		return null;
	}
	
	public RippleAccount getAccount(String name) {
		return accounts.get(name);
	}

	public Currency getCurrency(String abbrev) {
		return currencies.get(abbrev);
	}
	
	public Set<Debt> getDebtsTo(RippleAccount account) {
		return debtsToUser.get(account);
	}
	
	public Set<Trust> getTrustsTo(RippleAccount account) {
		return trustsToUser.get(account);
	}
	
	public Set<Debt> getDebtsFrom(RippleAccount account) {
		Set<Debt> result = new HashSet<Debt>();
		for (Set<Debt> debts : debtsToUser.values()) {
			for (Debt debt : debts) {
				if (debt.getOwedBy().equals(account.getAccountName())) {
					result.add(debt);
				}
			}
		}
		return result;
	}
	
	public Set<Trust> getTrustsFrom(RippleAccount account) {
		Set<Trust> result = new HashSet<Trust>();
		for (Set<Trust> trusts : trustsToUser.values()) {
			for (Trust trust : trusts) {
				if (trust.getTrustFrom().equals(account.getAccountName())) {
					result.add(trust);
				}
			}
		}
		return result;
	}
}
