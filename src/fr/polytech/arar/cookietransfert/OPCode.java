package fr.polytech.arar.cookietransfert;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum OPCode {
	RRQ(1),
	WRQ(2),
	DATA(3),
	ACK(4),
	ERROR(5);
	
	private int code;
	private String representation;
	
	OPCode(int code, String representation) {
		setCode(code);
		setRepresentation(representation);
	}
	OPCode(int code) {
		setCode(code);
		setRepresentation(name().toUpperCase());
	}
	
	/* GETTERS & SETTERS */
	
	@Contract(pure = true)
	public int getCode() {
		return code;
	}
	
	private void setCode(int code) {
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
