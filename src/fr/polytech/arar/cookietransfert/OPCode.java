package fr.polytech.arar.cookietransfert;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum OPCode {
	RRQ(1),
	WRQ(2),
	DATA(3),
	ACK(4),
	ERROR(5);
	
	private byte code;
	private String representation;
	
	OPCode(byte code, String representation) {
		setCode(code);
		setRepresentation(representation);
	}
	OPCode(int code, String representation) {
		this((byte) code, representation);
	}
	OPCode(byte code) {
		setCode(code);
		setRepresentation(name().toUpperCase());
	}
	OPCode(int code) {
		this((byte) code);
	}
	
	/* GETTERS & SETTERS */
	
	@Contract(pure = true)
	public byte getCode() {
		return code;
	}
	
	private void setCode(byte code) {
		this.code = code;
	}
	
	@Contract(pure = true)
	public String getRepresentation() {
		return representation;
	}
	
	@SuppressWarnings("ConstantConditions")
	private void setRepresentation(@NotNull String representation) {
		if (representation == null || "".equals(representation))
			throw new IllegalArgumentException("representation cannot be null or empty");
		
		this.representation = representation;
	}
}
