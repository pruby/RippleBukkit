package nz.net.goddard.mc.ripple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

public class Debt {
	private String owedBy;
	private String owedTo;
	private Currency currency;
	private BigDecimal amount;
	
	public Debt(String owedBy, String owedTo, Currency currency, BigDecimal amount) {
		super();
		this.owedBy = owedBy;
		this.owedTo = owedTo;
		this.currency = currency;
		this.amount = amount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getOwedBy() {
		return owedBy;
	}

	public String getOwedTo() {
		return owedTo;
	}

	public Currency getCurrency() {
		return currency;
	}
	
	public void save(ObjectOutputStream output) throws IOException {
		output.writeUTF(owedBy);
		output.writeUTF(owedTo);
		output.writeUTF(currency.getAbbreviation());
		output.writeObject(amount);
	}
	
	public static Debt load(RippleNetwork network, ObjectInputStream input) throws IOException, ClassNotFoundException {
		String owedBy = input.readUTF();
		String owedTo = input.readUTF();
		String currencyAbbrev = input.readUTF();
		Object amountOwed = input.readObject();
		if (!(amountOwed instanceof BigDecimal)) {
			throw new IOException("Invalid amount");
		}
		
		return new Debt(owedBy, owedTo, network.getCurrency(currencyAbbrev), (BigDecimal) amountOwed);
	}
}
