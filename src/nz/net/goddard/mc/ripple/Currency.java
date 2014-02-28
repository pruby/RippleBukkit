package nz.net.goddard.mc.ripple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Currency {
	private String abbreviation;
	private String description;

	public Currency(String abbreviation, String description) {
		super();
		this.abbreviation = abbreviation;
		this.description = description;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Currency)) {
			return false;
		}
		
		Currency other = (Currency) obj;
		return other.abbreviation.equals(abbreviation);
	}
	
	public void save(ObjectOutputStream output) throws IOException {
		output.writeUTF(abbreviation);
		output.writeUTF(description);
	}
	
	public static Currency load(ObjectInputStream input) throws IOException {
		String abbreviation = input.readUTF();
		String description = input.readUTF();
		
		return new Currency(abbreviation, description);
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getDescription() {
		return description;
	}

}
