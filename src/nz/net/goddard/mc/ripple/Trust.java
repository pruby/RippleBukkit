package nz.net.goddard.mc.ripple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

public class Trust {
	private String trustFrom;
	private String trustTo;
	private Currency currency;
	private BigDecimal amount;
	private boolean amountIsPercent;
	
	public Trust(String trustFrom, String trustTo, Currency currency, BigDecimal amount, boolean amountIsPercent) {
		super();
		this.trustFrom = trustFrom;
		this.trustTo = trustTo;
		this.currency = currency;
		this.amount = amount;
		this.amountIsPercent = amountIsPercent;
	}

	public Currency getCurrency() {
		return currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public boolean isAmountIsPercent() {
		return amountIsPercent;
	}
	public String getTrustFrom() {
		return trustFrom;
	}

	public String getTrustTo() {
		return trustTo;
	}
	
	public void save(ObjectOutputStream output) throws IOException {
		output.writeUTF(trustFrom);
		output.writeUTF(trustTo);
		output.writeUTF(currency.getAbbreviation());
		output.writeObject(amount);
		output.writeBoolean(amountIsPercent);
	}
	
	public static Trust load(RippleNetwork network, ObjectInputStream input) throws IOException, ClassNotFoundException {
		String trustFrom = input.readUTF();
		String trustTo = input.readUTF();
		String currencyAbbrev = input.readUTF();
		Object amount = input.readObject();
		boolean amountIsPercent = input.readBoolean();
		
		if (!(amount instanceof BigDecimal)) {
			throw new IOException("Invalid amount");
		}
		
		return new Trust(trustFrom, trustTo, network.getCurrency(currencyAbbrev), (BigDecimal) amount, amountIsPercent);
	}
}
