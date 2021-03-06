package de.rebleyama.lib.net.message;

/**
 * The Handshake between the client and server where the client receives an id from the server
 */
public class HandshakeMessage extends Message {
    private String hello = "HELLO";

    /**
     * Creates a handshake message for the server assigning the client an id.
     * @param clientID The client id generated by the server and assigned to a client
     */
    public HandshakeMessage(byte clientID) {
        this.msgType = MessageType.HANDSHAKE;
        this.clientID = clientID;
    }

    /**
     * Creates a handshake message on the client when it hasn't received an id yet.
     */
    public HandshakeMessage() {
        this((byte) 0);
    }
}
