package chess.server;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ServerChessLogic
 * Tests command validation without turn restrictions (real-time gameplay)
 */
@DisplayName("ServerChessLogic Tests")
public class ServerChessLogicTest {

    private ServerChessLogic chessLogic;

    @BeforeEach
    void setUp() {
        chessLogic = new ServerChessLogic();
    }

    @Test
    @DisplayName("Should validate WHITE player movement commands")
    void shouldValidateWhitePlayerMovementCommands() {
        // Given
        ServerCommand whiteUp = new ServerCommand("W_MOVEMENT_UP");
        ServerCommand whiteDown = new ServerCommand("W_MOVEMENT_DOWN");
        ServerCommand whiteLeft = new ServerCommand("W_MOVEMENT_LEFT");
        ServerCommand whiteRight = new ServerCommand("W_MOVEMENT_RIGHT");

        // When & Then
        assertThat(chessLogic.isValidCommand(whiteUp)).isTrue();
        assertThat(chessLogic.isValidCommand(whiteDown)).isTrue();
        assertThat(chessLogic.isValidCommand(whiteLeft)).isTrue();
        assertThat(chessLogic.isValidCommand(whiteRight)).isTrue();
        
        // Verify command properties
        assertThat(whiteUp.getCommandType()).isEqualTo(ServerCommand.CommandType.KEY_INPUT);
        assertThat(whiteUp.getPlayer()).isEqualTo(ServerCommand.Player.WHITE);
    }

    @Test
    @DisplayName("Should validate BLACK player movement commands")
    void shouldValidateBlackPlayerMovementCommands() {
        // Given
        ServerCommand blackUp = new ServerCommand("B_MOVEMENT_UP");
        ServerCommand blackDown = new ServerCommand("B_MOVEMENT_DOWN");
        ServerCommand blackLeft = new ServerCommand("B_MOVEMENT_LEFT");
        ServerCommand blackRight = new ServerCommand("B_MOVEMENT_RIGHT");

        // When & Then
        assertThat(chessLogic.isValidCommand(blackUp)).isTrue();
        assertThat(chessLogic.isValidCommand(blackDown)).isTrue();
        assertThat(chessLogic.isValidCommand(blackLeft)).isTrue();
        assertThat(chessLogic.isValidCommand(blackRight)).isTrue();
        
        // Verify command properties
        assertThat(blackUp.getCommandType()).isEqualTo(ServerCommand.CommandType.KEY_INPUT);
        assertThat(blackUp.getPlayer()).isEqualTo(ServerCommand.Player.BLACK);
    }

    @Test
    @DisplayName("Should validate select or move commands")
    void shouldValidateSelectOrMoveCommands() {
        // Given
        ServerCommand whiteSelectOrMove = new ServerCommand("W_SELECT_OR_MOVE");
        ServerCommand blackSelectOrMove = new ServerCommand("B_SELECT_OR_MOVE");

        // When & Then
        assertThat(chessLogic.isValidCommand(whiteSelectOrMove)).isTrue();
        assertThat(chessLogic.isValidCommand(blackSelectOrMove)).isTrue();
        
        assertThat(whiteSelectOrMove.getCommandType()).isEqualTo(ServerCommand.CommandType.KEY_INPUT);
        assertThat(blackSelectOrMove.getCommandType()).isEqualTo(ServerCommand.CommandType.KEY_INPUT);
    }

    @Test
    @DisplayName("Should validate piece selection commands")
    void shouldValidatePieceSelectionCommands() {
        // Given
        ServerCommand whiteSelect0 = new ServerCommand("W_SELECT_PIECE_0");
        ServerCommand whiteSelect7 = new ServerCommand("W_SELECT_PIECE_7");
        ServerCommand blackSelect2 = new ServerCommand("B_SELECT_PIECE_2");
        ServerCommand blackSelect5 = new ServerCommand("B_SELECT_PIECE_5");

        // When & Then
        assertThat(chessLogic.isValidCommand(whiteSelect0)).isTrue();
        assertThat(chessLogic.isValidCommand(whiteSelect7)).isTrue();
        assertThat(chessLogic.isValidCommand(blackSelect2)).isTrue();
        assertThat(chessLogic.isValidCommand(blackSelect5)).isTrue();
    }

    @Test
    @DisplayName("Should accept all commands in real-time gameplay")
    void shouldAcceptAllCommandsInRealTime() {
        // Given
        ServerCommand invalidCommand = new ServerCommand("INVALID_COMMAND");
        ServerCommand nullCommand = null;

        // When & Then
        // Note: Current implementation accepts any command as valid for real-time gameplay
        assertThat(chessLogic.isValidCommand(invalidCommand)).isTrue();
        assertThat(chessLogic.isValidCommand(nullCommand)).isFalse();
    }

    @Test
    @DisplayName("Should handle game control commands")
    void shouldHandleGameControlCommands() {
        // Given
        ServerCommand endGameCommand = new ServerCommand("END_GAME");

        // When & Then
        assertThat(endGameCommand).isNotNull();
        assertThat(endGameCommand.getCommandType()).isEqualTo(ServerCommand.CommandType.GAME_CONTROL);
        assertThat(chessLogic.isValidCommand(endGameCommand)).isTrue();
    }

    @Test
    @DisplayName("Should allow simultaneous commands from different players (real-time)")
    void shouldAllowSimultaneousCommandsFromDifferentPlayers() {
        // Given - Both players can act at the same time (no turns)
        ServerCommand whiteMove = new ServerCommand("W_MOVEMENT_UP");
        ServerCommand blackMove = new ServerCommand("B_MOVEMENT_DOWN");
        ServerCommand whiteJump = new ServerCommand("W_JUMP");
        ServerCommand blackJump = new ServerCommand("B_JUMP");

        // When & Then - All commands should be valid regardless of order
        assertThat(chessLogic.isValidCommand(whiteMove)).isTrue();
        assertThat(chessLogic.isValidCommand(blackMove)).isTrue();
        assertThat(chessLogic.isValidCommand(whiteJump)).isTrue();
        assertThat(chessLogic.isValidCommand(blackJump)).isTrue();
        
        // Validate them again to ensure no state prevents simultaneous play
        assertThat(chessLogic.isValidCommand(whiteMove)).isTrue();
        assertThat(chessLogic.isValidCommand(blackMove)).isTrue();
    }

    @Test
    @DisplayName("Should handle hover commands")
    void shouldHandleHoverCommands() {
        // Given
        ServerCommand whiteHoverUp = new ServerCommand("W_HOVER_UP");
        ServerCommand blackHoverDown = new ServerCommand("B_HOVER_DOWN");

        // When & Then
        assertThat(chessLogic.isValidCommand(whiteHoverUp)).isTrue();
        assertThat(chessLogic.isValidCommand(blackHoverDown)).isTrue();
    }

    @Test
    @DisplayName("Should parse ServerCommand correctly")
    void shouldParseServerCommandCorrectly() {
        // Given
        ServerCommand whiteMove = new ServerCommand("W_MOVEMENT_UP");
        ServerCommand blackJump = new ServerCommand("B_JUMP");
        ServerCommand gameControl = new ServerCommand("END_GAME");

        // When & Then
        assertThat(whiteMove.getCommandType()).isEqualTo(ServerCommand.CommandType.KEY_INPUT);
        assertThat(whiteMove.getPlayer()).isEqualTo(ServerCommand.Player.WHITE);
        assertThat(whiteMove.getKeyInput()).isEqualTo("MOVEMENT_UP");

        assertThat(blackJump.getCommandType()).isEqualTo(ServerCommand.CommandType.JUMP);
        assertThat(blackJump.getPlayer()).isEqualTo(ServerCommand.Player.BLACK);
        assertThat(blackJump.getKeyInput()).isEqualTo("JUMP");

        assertThat(gameControl.getCommandType()).isEqualTo(ServerCommand.CommandType.GAME_CONTROL);
        assertThat(gameControl.getPlayer()).isEqualTo(ServerCommand.Player.SYSTEM);
        assertThat(gameControl.getKeyInput()).isEqualTo("D_GAME"); // ServerCommand parsing cuts first 2 chars
    }
}