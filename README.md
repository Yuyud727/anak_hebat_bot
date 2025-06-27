🤖 My Telegram Fitness Bot
This Telegram bot is designed to assist users with fitness and nutrition information, as well as provide administrative tools for managing members and broadcasts. It supports both user-facing commands and a comprehensive admin panel.

✨ Features
For Users
Users can interact with the bot to get information related to fitness, nutrition, and general health news.

1. Welcome & Registration (/start, /register)

    - New users are greeted and prompted to register by providing their name and phone number.
    - Existing users are welcomed back and presented with the main menu.

2. Main Menu (/menu)

    - Provides quick access to different categories of information (Nutrition, Gym, News, Broadcasts).

3. Nutrition & Diet Information (/nutrisi)

    - Access a menu of nutrition-related keywords.
    - Keywords: Type specific keywords like protein, kalori, diet, karbohidrat, lemak, vitamin to receive detailed information.

4. Gym & Exercise Guides (/gym)

    - Access a menu of exercise-related keywords.
    - Keywords: Type specific keywords like squat, push up, pull up, deadlift, bench press, cardio for technique guides and tips.

5. Health & Fitness News (/berita)

    - Access a menu for health and fitness news.
    - Keywords: Type specific keywords like trending, research, tips, workout for the latest updates.

6. Recent Broadcasts (/broadcast)

    - View the 5 most recent broadcast messages sent by the admin.

7. General Keyword Responses

    - The bot can respond to predefined keywords (e.g., "protein", "squat") with relevant information. If a message isn't a command or a recognized keyword, the bot will prompt the user to use /menu.

For Admins
1. Admins have access to a powerful panel to manage members, send broadcasts, and monitor bot activity.

    - Admin Panel Access (/admin)
    - Displays a comprehensive menu of all available admin commands.

2. Member Management

    - /members: View a list of all active registered members, including their Chat ID, name, phone number, and registration date.
    - /addmember chatid|nama|telepon: Manually add a new member to the database. Includes input validation for Chat ID, name, and phone number, and checks for existing members. A welcome message is sent to the newly added member.
    - /deletemember chatid: Remove a member from the database using their Chat ID. Includes confirmation and a farewell message sent to the deleted member.

3. Broadcast Management

    - /broadcast kategori|judul|isi pesan: Send a broadcast message to all active members.

        - Categories: gym, nutrisi, berita.
        - The bot tracks the number of recipients and marks the broadcast as sent.

4. Bot Statistics (/stats)

    - Provides an overview of the bot's activity, including total members, active members, total broadcasts, and sent broadcasts.

5. Message Management

    - /messages [all|incoming|outgoing]: View message history.
        - all: Shows both incoming and outgoing messages.
        - incoming: Shows only incoming messages.
        - outgoing: Shows only outgoing messages.

    - /incoming [jumlah]: View the latest incoming messages (default: 10).
    - /outgoing [jumlah]: View the latest outgoing messages (default: 10).
    - /searchmessage kata_kunci: Search for messages containing a specific keyword.
    - /messagesbymember chat_id: View all messages exchanged with a specific member.
    - /clearmessages [all|older_than_days]: Clear message history.

        - all: Deletes all messages.

        - older_than_days: Deletes messages older than the specified number of days.

    - /exportmessages [all|incoming|outgoing]: Export message data (format not specified in code, but implies a function to do so).
    - /messagestats: Get statistics related to message traffic (e.g., total messages, incoming/outgoing counts).

🛠️ Setup and Configuration
The bot relies on a config.properties file for its token, username, admin chat ID, and database connection details (SQLite or MySQL). Ensure this file is correctly configured in your project's resources.

🚀 Usage
For Users
    - Start the bot: Send /start to the bot.
    - Register (if new): Follow the prompts to provide your name and phone number.
    - Explore: Use /menu to see available categories, or type keywords directly (e.g., protein, squat).

For Admins
    - Access Admin Panel: Send /admin from the configured admin chat ID.
    - Use Commands: Refer to the /admin menu for a list of available commands and their formats.

📚 Dependencies
    - Telegram Bots API
    - JDBC Driver for SQLite (built-in) or MySQL (e.g., MySQL Connector/J)
    - Java Development Kit (JDK) 11 or higher
>>>>>>> 234bc76 (Initial commit: Add MyTelegramBot project)
