package nz.net.goddard.mc.ripple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RippleAccount {
	private RippleNetwork network;
	private String accountName;

	public RippleAccount(RippleNetwork network, String accountName) {
		super();
		this.network = network;
		this.accountName = accountName;
	}
	
	public void save(ObjectOutputStream output) throws IOException {
		output.writeUTF(accountName);
	}
	
	public static RippleAccount load(RippleNetwork network, ObjectInputStream input) throws IOException {
		String accountName = input.readUTF();
		
		return new RippleAccount(network, accountName);
	}

	public String getAccountName() {
		return accountName;
	}
	
	private BigDecimal totalOwed(Currency currency) {
		BigDecimal total = new BigDecimal(0);
		for (Debt debt : network.getDebtsTo(this)) {
			if (debt.getCurrency().equals(currency)) {
				total = total.add(debt.getAmount());
			}
		}
		return total;
	}
	
	private BigDecimal totalOwing(Currency currency) {
		BigDecimal total = new BigDecimal(0);
		for (Debt debt : network.getDebtsFrom(this)) {
			if (debt.getCurrency().equals(currency)) {
				total = total.add(debt.getAmount());
			}
		}
		return total;
	}
	
	private Set<List<String>> findAllPaths(String to, Currency currency, BigDecimal amount) {
		Set<List<String>> pathList = new HashSet<List<String>>();
		Set<List<String>> edgeSet = new HashSet<List<String>>();
		List<String> initialPath = new ArrayList<String>();
		initialPath.add(to);
		edgeSet.add(initialPath);
		
		for (int i = 0; i < RippleNetwork.MAX_PATH_DEPTH && !edgeSet.isEmpty(); ++i) {
			Set<List<String>> nextSet = new HashSet<List<String>>();			
			for (List<String> path : edgeSet) {
				String lastAccount = path.get(path.size() - 1);
				
				List<String> trustors = network.getTrustors(lastAccount, currency);
				List<String> debtors = network.getDebtors(lastAccount, currency);
				
				for (String trustor : trustors) {
					if (!path.contains(trustor)) {
						List<String> newPath = new ArrayList<String>(path);
						newPath.add(trustor);
						
						if (trustor.equals(to)) {
							pathList.add(newPath);
						} else {
							edgeSet.add(newPath);
						}
					}
				}
				
				for (String debtor : debtors) {
					if (!path.contains(debtor)) {
						List<String> newPath = new ArrayList<String>(path);
						newPath.add(debtor);
						if (debtor.equals(to)) {
							pathList.add(newPath);
						} else {
							edgeSet.add(newPath);
						}
					}
				}
			}

			edgeSet = nextSet;
		}
		
		return pathList;
	}
	
	private BigDecimal findBottleneck(List<String> path, Currency currency, Set<List<String>> priorPaths) {
		String from = null;
		BigDecimal max = null;
		
		for (String account : path) {
			if (from == null) {
				// First element
				from = account;
			} else {
				Debt debt = network.getDebt(account, from, currency);
				Trust trust = network.getTrust(account, from, currency);
				
				BigDecimal neck = new BigDecimal(0);
				if (debt != null) {
					neck = debt.getAmount();
				}
				
				if (trust != null) {
					if (trust.isAmountIsPercent()) {
						if (trust.getAmount().compareTo(new BigDecimal(100)) > 0) {
							neck = null;
						} else {
							BigDecimal totalOwedNow = network.getAccount(account).totalOwed(currency);
							Debt existingDebt = network.getDebt(account, from, currency);
							BigDecimal owedByThisAcct;
							if (existingDebt != null) {
								owedByThisAcct = existingDebt.getAmount();
							} else {
								owedByThisAcct = new BigDecimal(0);
							}
							
							// TODO Calculate amount owable to reach percent
						}
					} else {
						
					}
				}
				
				from = account;
			}
		}
		
		return max;
	}
}
