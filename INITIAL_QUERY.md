Hi Junie. I want you to help me create an REST API application for calorie and exercise tracking.

The idea behind this application is to provide a basic API for AI assistants (mainly ChatGPT) that would allow them to track calories and exercises based on user's queries.

Below is a detailed description of the application features

# Functionality

## Calorie tracking
Calorie tracking should be done via daily food diary.
A diary consists of following:
* Owner
* Date
* Comment
* A list of entries
  A diary entry consists of following fields
* Ingredient/product/dish
* Amount (either mass, packs or items)
    * can be integer or a fraction, e.g. 1 pack or 1/2 pack
* Meal (breakfast, lunch, dinner, snack)
* Comment

The application should provide API endpoints for CRUD functionality over diary and entry. It should have data validation, e.g. not allow to add entry to non existing diary. It should not be possible to create 2 diaries for one date. Deleting a diary should also delete all entries.

## Food database
The application should have its own food database used in calorie tracking. A food database entry should consist of:
* Owner
  * Either user id or shared
  * Users can only access shared and their own food database entries
  * Shared food database entries can be accessed by all users 
* Type
    * Basic ingredient: like eggs, onions, meat, flour, sugar etc.
    * Homemade dish: a dish made at home
    * Restaraunt dish
    * Product: manufactured products from shops
* Name
    * Should support i18n
    * Should support full text search with fuzzy matching
* Macroses
    * Proteins, fats and carbohydrates per 100 grams
* Producer
    * Name of the manufacturer for products
    * Name of the restaraunt for restaraunt dishes
    * Should support full text search with fuzzy matching1
* Measurements
    * Pack -- the amount of grams in one pack
    * Item -- the mass of one item
        * for items that can have various weight (e.g. a big apple will have higher mass than small apple, but will still be 1 item) the mass should be an average
    * Cup, tablespoon, teaspoon -- a mass of one such volume
        * for items that can have volumetric measuremets, e.g. liquids, flour, rice, groats etc.
* Tags
    * additional arbitrary set of tags, like: fruit, vegetable, meat etc.
    * Should have an index to support search by tag

The application should provide API for CRUD operations over food database. A full database query or search should support pagination. Deletion should be soft, a food entry should only be marked as deleted and excluded from search results. It should not be possible to update a deleted entry. But it should be possible to read it via get by id.

## User data
The application should provide API for CRUD operations over user data. A user data consists of
* Name
* Gender
* Date of birth
* Current weight (in kilograms)
* Height (in centimeters)
* Activity level
  * Should be taken from a predefined list of activity levels
* Daily calorie goal

There should also be a separate storing of a user's weight history. A history entry should consist of
* Timestamp
* Weight (in kilograms)
* Comment

When requesting a user's data, the application should return the latest data. There should be a separate API for getting a user's weight history. When requesting user's data the application should also compute the user's BMI and estimated daily calorie intake based on their latest data.
It should be possible to delete weight history entries, but not user data. When adding weight history entry, the application should also update current weight in user data.

## Diary summary
The application should provide API for diary summary. A summary should be built based on diary and food database entries. It should return
* Owner
* Total calories consumed
* A percentage of daily calorie goal
* A percentage of daily calorie intake
* Macros
* A list of meals
  * Total calories consumed for each meal and percentages
  * Macros
  * A list of diary entries
    * Total calories consumed for each diary entry and percentages
    * Name of the food
    * The amount of diary entry in selected measurement and in grams
    * Macros


## Exercise tracking
Exercise tracking should be done via training logs.
A training log consists of following:
* Owner
* Date
* Comment
* A list of exercises

* A training log entry consists of following fields
* Exercise
* Duration (in minutes)
* Repetitions
* Weight (in kilograms)

## Exercise database
The application should have its own exercise database used in exercise tracking. An exercise database entry should consist of:* Owner
* Owner
  * Either user id or shared
  * Users can only access shared and their own exercise database entries
  * Shared exercise database entries can be accessed by all users
* Name
    * Should support i18n
    * Should support full text search with fuzzy matching
* A list of affected muscles
  * Name 
    * Muscle names should be hardcoded in the application
  * Should support search by affected muscles
* Tags
  * Various arbitrary tags, like: cardio, strength, flexibility, back, core etc.
  * Should support search by tag

The application should provide API for CRUD operations over exercise database. A full database query or search should support pagination. Deletion should be soft, an exercise entry should only be marked as deleted and excluded from search results. It should not be possible to update a deleted entry. But it should be possible to read it via get by id.

# Application requirements
Spring Boot Kotlin application with REST API and Gradle. Use existing project structure.
Should have separation between API model, business model and persistence layer.
Should have swagger documentation.

## Tests
Use JUnit 5, Mockito and AssertJ.
1. Unit tests -- should use mocked dependencies and have as close to 100% code coverage as possible

## Authentication
It should be possible to register a new user with a login and a password. A login should be unique. The table for user authentication details should be separate from the user data.
It should be possible to log in with a login and a password. When logging in, the application should return a JWT token without expiration data an with user id.

## Authorization
JWT token should protect all endpoints, except for authentication endpoints.
Some users might be designated as administrators.
Administrators should be able to CRUD over shared entries of food/exercise database.
Endpoints for shared entries should be separate from the endpoints for non-shared entries.

# Database requirements
Use PostgreSQL
Full-text search (FTS): tsvector + tsquery with ranking (ts_rank) and GIN indexes.
Typo tolerance / fuzzy: pg_trgm extension (trigram similarity) + GIN/GiST indexes.
There should be no complex keys.

# Deployment
Docker container.

Start with analyzing this prompt and improving it.
Make it better structured and more readable.
Make a list of suggestions for improvements. Point out flaws in the design.
Ask any questions on the contents of the prompt if there are any uncertainties.





