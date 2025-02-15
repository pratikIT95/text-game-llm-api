# GameRestController

`GameRestController` is a REST controller that manages the game lifecycle and interactions for users. It provides endpoints for starting a new game and continuing an existing game by sending prompts.

## Endpoints

### Start Game

#### `POST /start/{userId}`

Starts a new game for the specified user.

- **Path Variable**:
  - `userId` (String): The unique identifier for the user.

- **Response**:
  - Returns a `String` message indicating the initiation of the conversation for the game.

#### Example Request

```http
POST /start/johndoe
```
