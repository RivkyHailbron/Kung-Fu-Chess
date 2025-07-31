# KFChess - New Clean Architecture

## ארכיטקטורה מינימלית וברורה

### 🎯 עיקרון המחולק

**לקוח (Client):**
- אחראי רק על גרפיקה ותצוגה
- קולט קלט מקלדת
- מציג תוצאות

**שרת (Server):**  
- אחראי רק על לוגיקה ובדיקת חוקיות
- אין גרפיקה בשרת!
- אין GUI בשרת!

### 🔄 זרימת הפקודות החדשה

```
[Keyboard Input] 
       ↓
[Queue: Keyboard Input] 
       ↓  
[Thread: Send to Server]
       ↓
[Server Validates] 
       ↓
[Broadcast to All Clients]
       ↓
[Queue: Commands from Server]
       ↓
[Game Loop Execution]
```

### 📁 מבנה הקבצים

#### Client Side
- `ClientGame.java` - מנהל את שני התורים והתהליכונים
- `ChessClient.java` - תקשורת עם השרת

#### Server Side  
- `ChessServer.java` - מנהל חיבורים וsocket תקשורת
- `ServerChessLogic.java` - לוגיקה מינימלית בלבד (ללא גרפיקה!)

### 🧵 Threading Architecture

#### Client Threads:
1. **Main Thread** - Game loop + GUI
2. **Server Sender Thread** - קוראת מתור הקלדת ושולחת לשרת  
3. **Command Processor Thread** - קוראת פקודות מאושרות מהשרת ומבצעת במשחק

#### Server Threads:
1. **Main Thread** - מקבל חיבורים
2. **Client Handler Threads** - אחד לכל לקוח, מעבד פקודות

### 🔍 Debug Output

הארכיטקטורה כוללת הדפסות debugging ברורות:

- `✓ [KEYBOARD → QUEUE]` - פקודה נכנסת לתור הקלדת
- `✓ [QUEUE → SERVER]` - פקודה נשלחת לשרת  
- `🔍 [SERVER VALIDATION]` - שרת בודק חוקיות
- `✓ [VALIDATION PASSED]` - פקודה עברה אימות
- `✓ [SERVER BROADCAST]` - שרת משדר לכל הלקוחות
- `✓ [SERVER → GAME]` - פקודה מתבצעת במשחק

### 💡 יתרונות הארכיטקטורה החדשה

1. **הפרדה ברורה** - לקוח = גרפיקה, שרת = לוגיקה
2. **ביצועים טובים** - השרת לא טוען גרפיקה מיותרת  
3. **אמינות** - כל פקודה עוברת דרך השרת
4. **סינכרון** - כל השחקנים רואים בדיוק את אותו דבר
5. **הרחבה קלה** - קל להוסיף שחקנים או features

### 🚫 מה השרת לא צריך יותר

- ❌ `GraphicsFactory` - אין גרפיקה בשרת
- ❌ `PhysicsFactory` - אין אנימציות בשרת  
- ❌ `Game` class מלא - רק לוגיקה בסיסית
- ❌ GUI components - השרת headless

### ✅ מה השרת כן צריך

- ✅ `Command` parsing - הבנת פקודות
- ✅ Board state - מצב הלוח הנוכחי  
- ✅ Rules validation - בדיקת חוקיות מהלכים
- ✅ Broadcasting - שידור לכל הלקוחות

## הרצה

1. הפעל שרת: `mvn -f chess-server/pom.xml exec:java`
2. הפעל לקוח 1: `mvn -f chess-client/pom.xml exec:java`  
3. הפעל לקוח 2: `mvn -f chess-client/pom.xml exec:java`

המשחק יתחיל רק כאשר שני שחקנים מחוברים.
