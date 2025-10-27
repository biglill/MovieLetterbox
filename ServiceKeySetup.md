Project Setup Guide for Team Members

Welcome to the project! To run this application on your local machine, you need to connect it to our shared Firebase database. This guide will walk you through the secure setup process.

The core principle is that each team member generates their own secret key. We will NEVER share the serviceAccountKey.json file with each other or commit it to the Git repository.

Step 1: Get Access and Generate Your Key

Before you start, a project owner must add you as a team member to the Firebase project.

Once you have access, go to our project in the Firebase Console.

In the top-left, click the Gear icon ⚙️ next to Project Overview and select Project settings.

Go to the Service accounts tab.

Click the Generate new private key button. A confirmation warning will appear; click Generate key.

A JSON file (e.g., movieletterbox-firebase-adminsdk-....json) will be downloaded. This is your personal key.

Step 2: Store Your Key Securely

Create a permanent, safe folder on your computer OUTSIDE of the project folder. This is where you will keep your secret keys.

Windows Example: C:\Users\YourName\Documents\GoogleKeys\

macOS Example: /Users/yourname/Documents/GoogleKeys/

Move the .json file you just downloaded into this new folder.

Rename the file to exactly serviceAccountKey.json.

Step 3: Set the Environment Variable in IntelliJ

Our code uses an environment variable to find your key file. You must set this variable directly in IntelliJ for it to work correctly when you run the app.

Copy the File Path: Find your serviceAccountKey.json file, right-click it, and choose Copy as path (Windows) or Copy Pathname (macOS, may need to hold the Option key).

Edit IntelliJ Configurations:

In IntelliJ, go to the top menu: Run -> Edit Configurations....

Make sure HelloApplication is selected on the left.

Find the Environment variables field and click the small icon at the end of the line to open the editor.

Add the Variable:

Click the plus icon (+).

Name: GOOGLE_APPLICATION_CREDENTIALS

Value: Paste the full, absolute path you copied.

IMPORTANT:

The path must be absolute (e.g., starting with C:\ or /Users/).

Do NOT put quotes (") around the path.

Do NOT use backslashes (\) to escape spaces if you're on a Mac. IntelliJ handles spaces automatically.

Click OK, then Apply, then OK to save the configuration.

Step 4: Run the Application

You're all set!

Make sure you have the latest version of the code from our Git repository.

Open the project in IntelliJ.

Run the HelloApplication.java file.

The application should now launch and successfully connect to the Firebase database.

Troubleshooting Common Errors

If you encounter an error, it's likely one of these:

Error: FileNotFoundException: ... (No such file or directory)

Cause: The path in your IntelliJ Run Configuration is incorrect.

Solution: Go back to Step 3. Double-check for typos, make sure it's the full absolute path, and ensure there are no quotes or extra slashes.

Error: Invalid JWT Signature

Cause: Your computer's clock is out of sync with Google's servers.

Solution: Open your computer's Date & Time settings, enable "Set time automatically," and click "Sync now." Then, restart IntelliJ.

Error: The GOOGLE_APPLICATION_CREDENTIALS environment variable is not set.

Cause: You either missed Step 3 or didn't click "Apply" and "OK" to save the Run Configuration.

Solution: Go back to Step 3 and carefully follow the instructions to set the variable inside IntelliJ.
