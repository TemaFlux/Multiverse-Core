package com.onarandombox.MultiverseCore.utils.webpaste;

/**
 * Thrown when pasting fails.
 */
public class PasteFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	public PasteFailedException() {
        super();
    }

    public PasteFailedException(Throwable cause) {
        super(cause);
    }
}
