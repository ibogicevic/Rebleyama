package de.rebleyama.lib.net.message;

/**
 * The Handshake between the client and server where the client receives an id from the server
 */
public class HandshakeMessage extends Message {
    private String hello = "HELLO";
    private  String playerName;

    /**
     * Creates a handshake message for the server assigning the client an id.
     * @param clientID The client id generated by the server and assigned to a client
     */
    public HandshakeMessage(byte clientID, String playerName) {
        this.msgType = MessageType.HANDSHAKE;
        this.clientID = clientID;
        this.playerName = playerName;
    }

    /**
     * Creates a handshake message on the client when it hasn't received an id yet.
     */
    public HandshakeMessage(String playerName) {
        this((byte) 0, playerName);
    }

    /**
     * Returns the self given name of the player
     * @return The name of the player
     */
    public String getPlayerName() {
        return playerName;
    }
}
