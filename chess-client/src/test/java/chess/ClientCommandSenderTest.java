package chess;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kamatech.chess.Command;

/**
 * Unit tests for ClientCommandSender
 * Tests command sending functionality (without mocks due to Java 23 issues)
 */
@DisplayName("ClientCommandSender Tests")
public class ClientCommandSenderTest {

    private TestChessClient testClient;
    private ClientCommandSender commandSender;
    private BlockingQueue<Command> commandQueue;

    /**
     * Simple test implementation of ChessClient for testing
     */
    private static class TestChessClient extends ChessClient {
        private final BlockingQueue<String> sentCommands = new LinkedBlockingQueue<>();
        
        public TestChessClient() {
            super(); // Use the default constructor
        }

        @Override
        public void sendCommandToServer(String command) {
            sentCommands.offer(command);
        }

        public BlockingQueue<String> getSentCommands() {
            return sentCommands;
        }
    }

    @BeforeEach
    void setUp() {
        testClient = new TestChessClient();
        commandQueue = new LinkedBlockingQueue<>();
        commandSender = new ClientCommandSender(testClient, Command.Player.WHITE, commandQueue);
    }

    @Test
    @DisplayName("Should send commands from queue to client")
    void shouldSendCommandsFromQueue() throws InterruptedException {
        // Given
        Command testCommand = Command.createKeyInput("MOVEMENT_UP", Command.Player.WHITE);

        // When
        commandSender.queueCommand(testCommand);
        commandSender.start();
        
        // Give some time for the command to be processed
        Thread.sleep(200);
        commandSender.stop();

        // Then
        assertThat(testClient.getSentCommands()).contains("W_MOVEMENT_UP");
    }

    @Test
    @DisplayName("Should handle multiple commands in sequence")
    void shouldHandleMultipleCommands() throws InterruptedException {
        // Given
        Command command1 = Command.createKeyInput("MOVEMENT_UP", Command.Player.WHITE);
        Command command2 = Command.createKeyInput("MOVEMENT_DOWN", Command.Player.WHITE);
        Command command3 = Command.createKeyInput("SELECT_OR_MOVE", Command.Player.WHITE);

        // When
        commandSender.queueCommand(command1);
        commandSender.queueCommand(command2);
        commandSender.queueCommand(command3);
        
        commandSender.start();
        Thread.sleep(300);
        commandSender.stop();

        // Then
        BlockingQueue<String> sentCommands = testClient.getSentCommands();
        assertThat(sentCommands).contains("W_MOVEMENT_UP", "W_MOVEMENT_DOWN", "W_SELECT_OR_MOVE");
    }

    @Test
    @DisplayName("Should handle JUMP commands correctly")
    void shouldHandleJumpCommands() throws InterruptedException {
        // Given
        Command jumpCommand = Command.createKeyInput("JUMP", Command.Player.WHITE);

        // When
        commandSender.queueCommand(jumpCommand);
        commandSender.start();
        Thread.sleep(200);
        commandSender.stop();

        // Then
        assertThat(testClient.getSentCommands()).contains("W_JUMP");
    }

    @Test
    @DisplayName("Should handle game control commands")
    void shouldHandleGameControlCommands() throws InterruptedException {
        // Given
        Command endGameCommand = Command.createGameControl("END_GAME");

        // When
        commandSender.queueCommand(endGameCommand);
        commandSender.start();
        Thread.sleep(200);
        commandSender.stop();

        // Then
        assertThat(testClient.getSentCommands()).contains("END_GAME");
    }

    @Test
    @DisplayName("Should start and stop correctly")
    void shouldStartAndStopCorrectly() throws InterruptedException {
        // When & Then
        assertThat(commandSender.isRunning()).isFalse();
        
        commandSender.start();
        Thread.sleep(100);
        assertThat(commandSender.isRunning()).isTrue();
        
        commandSender.stop();
        Thread.sleep(100);
        assertThat(commandSender.isRunning()).isFalse();
    }

    @Test
    @DisplayName("Should handle piece selection commands")
    void shouldHandlePieceSelectionCommands() throws InterruptedException {
        // Given
        Command selectPiece0 = Command.createKeyInput("SELECT_PIECE_0", Command.Player.WHITE);
        Command selectPiece4 = Command.createKeyInput("SELECT_PIECE_4", Command.Player.WHITE);

        // When
        commandSender.queueCommand(selectPiece0);
        commandSender.queueCommand(selectPiece4);
        
        commandSender.start();
        Thread.sleep(300);
        commandSender.stop();

        // Then
        BlockingQueue<String> sentCommands = testClient.getSentCommands();
        assertThat(sentCommands).contains("W_SELECT_PIECE_0", "W_SELECT_PIECE_4");
    }

    @Test
    @DisplayName("Should queue commands even when not running")
    void shouldQueueCommandsEvenWhenNotRunning() {
        // Given
        Command testCommand = Command.createKeyInput("MOVEMENT_UP", Command.Player.WHITE);

        // When
        commandSender.queueCommand(testCommand);

        // Then
        assertThat(commandQueue).hasSize(1);
        assertThat(commandQueue.peek()).isEqualTo(testCommand);
    }
}
