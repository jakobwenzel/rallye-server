# push shutdown to admins

# revisions for all static content
* atomic revisions on DB
* last changed on files
* push out notifications on revision change
* on startup push all revisions to all clients

# PictureLinks
* save all links to db, to guarantee consistency

# Database
* merge messages and chats (the separation might be relational, but only pays off if we scan for existing messages)

# Misc
* reduce logging in auth filters
* guarantee Userentry for all chats!! -> NEVER delete users