package chess;

import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kamatech.chess.Command;

/**
 * Unit tests for KeyboardCommandConverter
 * Tests keyboard input conversion to commands with different player colors
 */
@DisplayName("KeyboardCommandConverter Tests")
public class KeyboardCommandConverterTest {

    private KeyboardCommandConverter whiteConverter;
    private KeyboardCommandConverter blackConverter;
    private JPanel testComponent;

    @BeforeEach
    void setUp() {
        testComponent = new JPanel();
        whiteConverter = new KeyboardCommandConverter(Command.Player.WHITE);
        blackConverter = new KeyboardCommandConverter(Command.Player.BLACK);
    }

    @Test
    @DisplayName("Should convert WHITE player WASD movement keys correctly")
    void shouldConvertWhiteMovementKeys() {
        // Given - WHITE player movement keys
        KeyEvent keyW = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_W, 'w');
        KeyEvent keyA = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_A, 'a');
        KeyEvent keyS = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_S, 's');
        KeyEvent keyD = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_D, 'd');

        // When
        Command cmdW = whiteConverter.convertKeyEventToCommand(keyW);
        Command cmdA = whiteConverter.convertKeyEventToCommand(keyA);
        Command cmdS = whiteConverter.convertKeyEventToCommand(keyS);
        Command cmdD = whiteConverter.convertKeyEventToCommand(keyD);

        // Then
        assertThat(cmdW).isNotNull();
        assertThat(cmdW.getPlayer()).isEqualTo(Command.Player.WHITE);
        assertThat(cmdW.getKeyInput()).isEqualTo("MOVEMENT_UP");
        assertThat(cmdW.getRawCommand()).isEqualTo("W_MOVEMENT_UP");

        assertThat(cmdA).isNotNull();
        assertThat(cmdA.getKeyInput()).isEqualTo("MOVEMENT_LEFT");
        assertThat(cmdA.getRawCommand()).isEqualTo("W_MOVEMENT_LEFT");

        assertThat(cmdS).isNotNull();
        assertThat(cmdS.getKeyInput()).isEqualTo("MOVEMENT_DOWN");
        assertThat(cmdS.getRawCommand()).isEqualTo("W_MOVEMENT_DOWN");

        assertThat(cmdD).isNotNull();
        assertThat(cmdD.getKeyInput()).isEqualTo("MOVEMENT_RIGHT");
        assertThat(cmdD.getRawCommand()).isEqualTo("W_MOVEMENT_RIGHT");
    }

    @Test
    @DisplayName("Should convert BLACK player Arrow Keys correctly")
    void shouldConvertBlackMovementKeys() {
        // Given - BLACK player movement keys
        KeyEvent keyUp = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
        KeyEvent keyDown = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        KeyEvent keyLeft = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);
        KeyEvent keyRight = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);

        // When
        Command cmdUp = blackConverter.convertKeyEventToCommand(keyUp);
        Command cmdDown = blackConverter.convertKeyEventToCommand(keyDown);
        Command cmdLeft = blackConverter.convertKeyEventToCommand(keyLeft);
        Command cmdRight = blackConverter.convertKeyEventToCommand(keyRight);

        // Then
        assertThat(cmdUp).isNotNull();
        assertThat(cmdUp.getPlayer()).isEqualTo(Command.Player.BLACK);
        assertThat(cmdUp.getKeyInput()).isEqualTo("MOVEMENT_UP");
        assertThat(cmdUp.getRawCommand()).isEqualTo("B_MOVEMENT_UP");

        assertThat(cmdDown).isNotNull();
        assertThat(cmdDown.getKeyInput()).isEqualTo("MOVEMENT_DOWN");
        assertThat(cmdDown.getRawCommand()).isEqualTo("B_MOVEMENT_DOWN");

        assertThat(cmdLeft).isNotNull();
        assertThat(cmdLeft.getKeyInput()).isEqualTo("MOVEMENT_LEFT");
        assertThat(cmdLeft.getRawCommand()).isEqualTo("B_MOVEMENT_LEFT");

        assertThat(cmdRight).isNotNull();
        assertThat(cmdRight.getKeyInput()).isEqualTo("MOVEMENT_RIGHT");
        assertThat(cmdRight.getRawCommand()).isEqualTo("B_MOVEMENT_RIGHT");
    }

    @Test
    @DisplayName("Should convert action keys correctly for each player")
    void shouldConvertActionKeys() {
        // Given
        KeyEvent spaceKey = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
        KeyEvent enterKey = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');

        // When
        Command whiteSpace = whiteConverter.convertKeyEventToCommand(spaceKey);
        Command blackEnter = blackConverter.convertKeyEventToCommand(enterKey);

        // Then
        assertThat(whiteSpace).isNotNull();
        assertThat(whiteSpace.getPlayer()).isEqualTo(Command.Player.WHITE);
        assertThat(whiteSpace.getKeyInput()).isEqualTo("SELECT_OR_MOVE");
        assertThat(whiteSpace.getRawCommand()).isEqualTo("W_SELECT_OR_MOVE");

        assertThat(blackEnter).isNotNull();
        assertThat(blackEnter.getPlayer()).isEqualTo(Command.Player.BLACK);
        assertThat(blackEnter.getKeyInput()).isEqualTo("SELECT_OR_MOVE");
        assertThat(blackEnter.getRawCommand()).isEqualTo("B_SELECT_OR_MOVE");
    }

    @Test
    @DisplayName("Should convert JUMP commands correctly")
    void shouldConvertJumpCommands() {
        // Given - Use constructor that includes key location
        // For JUMP, the logic is: LEFT SHIFT for WHITE, RIGHT SHIFT for BLACK
        // But since determinePlayerFromKeyCode doesn't handle VK_SHIFT,
        // the converter should use myPlayerColor instead of detecting from keycode
        KeyEvent leftShift = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_LEFT);
        
        KeyEvent rightShift = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED, KeyEvent.KEY_LOCATION_RIGHT);

        // When
        Command whiteJump = whiteConverter.convertKeyEventToCommand(leftShift);
        Command blackJump = blackConverter.convertKeyEventToCommand(rightShift);

        // Then - JUMP should work based on KEY_LOCATION matching player
        assertThat(whiteJump).isNotNull();
        assertThat(whiteJump.getPlayer()).isEqualTo(Command.Player.WHITE);
        assertThat(whiteJump.getKeyInput()).isEqualTo("JUMP");
        assertThat(whiteJump.getRawCommand()).isEqualTo("W_JUMP");

        assertThat(blackJump).isNotNull();
        assertThat(blackJump.getPlayer()).isEqualTo(Command.Player.BLACK);
        assertThat(blackJump.getKeyInput()).isEqualTo("JUMP");
        assertThat(blackJump.getRawCommand()).isEqualTo("B_JUMP");
        
        // Also test wrong combinations should return null
        Command wrongJumpWhite = whiteConverter.convertKeyEventToCommand(rightShift);  // WHITE player with RIGHT shift
        Command wrongJumpBlack = blackConverter.convertKeyEventToCommand(leftShift);   // BLACK player with LEFT shift
        
        assertThat(wrongJumpWhite).isNull();
        assertThat(wrongJumpBlack).isNull();
    }

    @Test
    @DisplayName("Should ignore keys for other players")
    void shouldIgnoreOtherPlayerKeys() {
        // Given - WHITE converter with BLACK player keys
        KeyEvent blackArrowUp = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
        KeyEvent blackEnter = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, '\n');

        // When
        Command ignoredArrow = whiteConverter.convertKeyEventToCommand(blackArrowUp);
        Command ignoredEnter = whiteConverter.convertKeyEventToCommand(blackEnter);

        // Then
        assertThat(ignoredArrow).isNull();
        assertThat(ignoredEnter).isNull();
    }

    @Test
    @DisplayName("Should return null for unsupported keys")
    void shouldReturnNullForUnsupportedKeys() {
        // Given
        KeyEvent unsupportedKey = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED);

        // When
        Command result = whiteConverter.convertKeyEventToCommand(unsupportedKey);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should convert number keys for piece selection")
    void shouldConvertNumberKeysForPieceSelection() {
        // Given
        KeyEvent key1 = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_1, '1');
        KeyEvent key5 = new KeyEvent(testComponent, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_5, '5');

        // When
        Command cmd1 = whiteConverter.convertKeyEventToCommand(key1);
        Command cmd5 = blackConverter.convertKeyEventToCommand(key5);

        // Then
        assertThat(cmd1).isNotNull();
        assertThat(cmd1.getPlayer()).isEqualTo(Command.Player.WHITE);
        assertThat(cmd1.getKeyInput()).isEqualTo("SELECT_PIECE_0");
        assertThat(cmd1.getRawCommand()).isEqualTo("W_SELECT_PIECE_0");

        assertThat(cmd5).isNotNull();
        assertThat(cmd5.getPlayer()).isEqualTo(Command.Player.BLACK);
        assertThat(cmd5.getKeyInput()).isEqualTo("SELECT_PIECE_4");
        assertThat(cmd5.getRawCommand()).isEqualTo("B_SELECT_PIECE_4");
    }
}
