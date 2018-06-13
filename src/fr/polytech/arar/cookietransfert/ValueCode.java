package fr.polytech.arar.cookietransfert;

import org.jetbrains.annotations.Contract;

public enum ValueCode {
	OK(0),
	TRANSFER_ERROR(1),
	ADDRESS_NOT_REACHABLE(2),
	CANNOT_SEND_ACK(3),
	LOCAL_ERROR(-1),
	CANNOT_CREATE_LOCAL_FILE(-2),
	CANNOT_READ_LOCAL_FILE(-3),
	CANNOT_WRITE_LOCAL_FILE(-4),
	CANNOT_DELETE_LOCAL_FILE(-5);
	
	private int code;
	
	ValueCode(int code) {
		setCode(code);
	}
	
	/* GETTER & SETTER */
	
	@Contract(pure = true)
	public int getCode() {
		return code;
	}
	
	private void setCode(int code) {
		this.code = code;
	}
}
