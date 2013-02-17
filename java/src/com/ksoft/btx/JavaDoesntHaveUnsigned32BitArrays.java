package com.ksoft.btx;

public class JavaDoesntHaveUnsigned32BitArrays extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	JavaDoesntHaveUnsigned32BitArrays() {
		super(
				"Java doesn't have unsigned 32-bit integers, but this BTX object requires an array that contains more than 2^31 characters. The API must be updated in order to correctly fix this.");
	}
}
