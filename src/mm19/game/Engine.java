package mm19.game;

import java.util.ArrayList;
import java.util.logging.Level;

import mm19.api.API;
import mm19.api.Action;
import mm19.api.HitReport;
import mm19.api.PlayerTurn;
import mm19.api.ShipActionResult;
import mm19.api.ShipData;
import mm19.api.SonarReport;
import mm19.exceptions.EngineException;
import mm19.exceptions.InputException;
import mm19.exceptions.ResourceException;
import mm19.game.board.Position;
import mm19.game.player.Player;
import mm19.game.ships.DestroyerShip;
import mm19.game.ships.MainShip;
import mm19.game.ships.PilotShip;
import mm19.game.ships.Ship;
import mm19.server.Server;

/**
 * @author mm19
 * 
 *         This will put all the pieces of the game together, and actually make
 *         things run.
 */

public class Engine {
	public static final int TURN_LIMIT = 10000;
	private API api;
	private Player[] players;
	private int turn = 0;
	private boolean started = false;
	private boolean winner = false;

	/**
	 * Constructor
	 */
	public Engine() {
		players = new Player[Constants.PLAYER_COUNT];
		for (int i = 0; i < Constants.PLAYER_COUNT; i++) {
			players[i] = null;
		}

		turn = 0;
	}
	
	public void setAPI(API api) {
		this.api = api;
	}

	/**
	 * Determines who has won a tie game The player with the highest total ship
	 * health wins If tied, the player with the most resources wins If tied,
	 * player 2 wins
	 * 
	 * @param p1
	 *            The first player that tied
	 * @param p2
	 *            The second player that tied
	 * @return returns the player that has been chosen as victor
	 */
	public static Player breakTie(Player p1, Player p2) {
		int p1Health = 0;
		int p2Health = 0;
		ArrayList<Ship> ships = p1.getBoard().getShips();
		for (Ship ship : ships) {
			if (ship.canGenerateResources()) {
				p1Health += ship.getHealth();
			}
		}
		ships = p2.getBoard().getShips();
		for (Ship ship : ships) {
			if (ship.canGenerateResources()) {
				p2Health += ship.getHealth();
			}
		}
		if (p1Health > p2Health)
			return p1;
		else if (p2Health > p1Health)
			return p2;

		if (p1.getResources() > p2.getResources())
			return p1;
		else if (p2.getResources() > p1.getResources())
			return p2;

		return p2;
	}

	/**
	 * Helper for handling the exceptions thrown by the Ability class
	 * 
	 * @param e
	 *            The EngineException thrown
	 * @param playerID
	 *            The current player's id
	 * @param action
	 *            The action attempted when the exception was thrown.
	 * @param turnResults
	 *            The results object to update
	 */
	private void handleEngineException(EngineException e, int playerID,
			Action action) {
		if(api == null) {
			setAPI(API.getAPI());
		}
		
		if (e instanceof InputException) {
			api.getPlayerTurn(playerID)
					.addShipActionResult(
							new ShipActionResult(
									action.shipID,
									ShipActionResult.ActionResult.INCORRECT_PARAMETERS));
		} else if (e instanceof ResourceException) {
			api.getPlayerTurn(playerID)
					.addShipActionResult(
							new ShipActionResult(
									action.shipID,
									ShipActionResult.ActionResult.INSUFFICIENT_RESOURCES));
		}
		api.addPlayerError(playerID, e.getMessage());
	}

	/**
	 * Sets up a player within the Engine
	 * 
	 * @param shipDatas
	 *            The ships to be placed
	 * @param playerToken
	 *            The player's authentication token
	 * @return The new player object
	 * @throws EngineException
	 */
	public Player setPlayer(ArrayList<ShipData> shipDatas, String playerToken)
			throws EngineException {

		if(api == null) {
			setAPI(API.getAPI());
		}
		
		if (started) {
			throw new EngineException(
					"The game has already started, can't add new player!");
		}

		ArrayList<Ship> ships = new ArrayList<Ship>();
		ArrayList<Position> positions = new ArrayList<Position>();

		Ship tempShip;
		Position tempPos;
		String tempType;

		for (int i = 0; i < shipDatas.size(); i++) {
			tempType = shipDatas.get(i).type;
			tempShip = null;
			if (tempType.equals(DestroyerShip.IDENTIFIER)) {
				tempShip = new DestroyerShip();
			} else if (tempType.equals(MainShip.IDENTIFIER)) {
				tempShip = new MainShip();
			} else if (tempType.equals(PilotShip.IDENTIFIER)) {
				tempShip = new PilotShip();
			}
			if (tempShip != null) {
				if (shipDatas.get(i).orientation == Position.Orientation.HORIZONTAL) {
					tempPos = new Position(shipDatas.get(i).xCoord,
							shipDatas.get(i).yCoord,
							Position.Orientation.HORIZONTAL);
				} else {
					tempPos = new Position(shipDatas.get(i).xCoord,
							shipDatas.get(i).yCoord,
							Position.Orientation.VERTICAL);
				}

				ships.add(tempShip);
				positions.add(tempPos);
			} else {
				throw new EngineException("Unable to initialize ship " + i
						+ " to type " + shipDatas.get(i).type);
			}
		}

		// Note: playerIDs are just incremented starting at 0, use this to our
		// advantage
		Player player = new Player(Constants.STARTING_RESOURCES);
		boolean setupShips = Ability.setupBoard(player, ships, positions);

		if (!setupShips) {
			throw new EngineException(
					"Unable to setup ships due to bad positions");
		}

		players[player.getPlayerID()] = player;

		// Set that the game is "started"
		turn++;
		if (turn > Constants.PLAYER_COUNT - 1) {
			started = true;
		}

		return player;
	}

	/**
	 * Processes a player's turn
	 * 
	 * @param playerToken
	 *            The player's authentication token
	 * @param actions
	 *            The list of actions to be performed by the player
	 * @return
	 */
	public synchronized boolean playerTurn(int playerID, ArrayList<Action> actions) {

		if(api == null) {
			setAPI(API.getAPI());
		}
		
		// Check for valid playerID
		// This, at the moment, is a redundant check (it is checked at the
		// Server level), we may want to keep it here as well or not.
		if (playerID != getCurrPlayerID()) {
			api.addPlayerError(playerID, "It is not your turn!");
			return false;
		}

		Player player = players[playerID];
		Player opponent = players[getCurrOpponentID()];

		Ability.gatherResources(player);

		PlayerTurn playerTurn = api.getPlayerTurn(player.getPlayerID());
		PlayerTurn opponentPlayerTurn = api.getPlayerTurn(opponent
				.getPlayerID());

		for (Action action : actions) {
			if (action.actionID == Action.Type.SHOOT) {
				try {
					HitReport hitResponse = Ability
							.shoot(player, opponent, action.shipID,
									action.actionXVar, action.actionYVar);
					playerTurn.addShipActionResult(new ShipActionResult(
							action.shipID,
							ShipActionResult.ActionResult.SUCCESS));
					playerTurn.addAction(action);
					playerTurn.addHitReport(hitResponse);
					if(hitResponse.shotSuccessful) {
						opponentPlayerTurn.addHitReport(hitResponse);
					}
				} catch (EngineException e) {
					handleEngineException(e, playerID, action);
				}
			} else if (action.actionID == Action.Type.BURST_SHOT) {
				try {
					ArrayList<HitReport> burstResponse = Ability.burstShot(
							player, opponent, action.shipID, action.actionXVar,
							action.actionYVar);
					playerTurn.addShipActionResult(new ShipActionResult(
							action.shipID,
							ShipActionResult.ActionResult.SUCCESS));
					playerTurn.addAction(action);
					for (HitReport hitReport : burstResponse) {
						if (hitReport.shotSuccessful) {
							opponentPlayerTurn.addHitReport(hitReport);
						}
					}
				} catch (EngineException e) {
					handleEngineException(e, playerID, action);
				}
			} else if (action.actionID == Action.Type.SONAR) {
				try {
					ArrayList<SonarReport> sonarResponse = Ability.sonar(
							player, opponent, action.shipID, action.actionXVar,
							action.actionYVar);
					playerTurn.addShipActionResult(new ShipActionResult(
							action.shipID,
							ShipActionResult.ActionResult.SUCCESS));
					playerTurn.addAction(action);

					for (SonarReport report : sonarResponse) {
						playerTurn.addSonarReport(report);
						opponentPlayerTurn.addSonarReport(new SonarReport(-1,
								report.ship));
					}
				} catch (EngineException e) {
					handleEngineException(e, playerID, action);
				}
			} else if (action.actionID == Action.Type.MOVE_HORIZONTAL
					|| action.actionID == Action.Type.MOVE_VERTICAL) {
				Position.Orientation orientation;
				if (action.actionID == Action.Type.MOVE_HORIZONTAL) {
					orientation = Position.Orientation.HORIZONTAL;
				} else {
					orientation = Position.Orientation.VERTICAL;
				}

				try {
					Ability.move(player, action.shipID, new Position(
							action.actionXVar, action.actionYVar, orientation));
					playerTurn.addShipActionResult(new ShipActionResult(
							action.shipID,
							ShipActionResult.ActionResult.SUCCESS));
					playerTurn.addAction(action);
				} catch (EngineException e) {
					handleEngineException(e, playerID, action);
				}
			}
		}
		
		Ability.resetAbilityStates(player);
		
		if (!opponent.isAlive()) {
			playerTurn.setWon();
			opponentPlayerTurn.setLost();
			
			Server.serverLog.log(Level.INFO, "Player " + playerTurn.getPlayer().getName() + " has won! (Opponent killed)");
			winner = true;
		}
		else if (turn > TURN_LIMIT) { // I changed this to an else for now so both players can't be defeated. Feel free to tweak in the future. - Ace
			player = breakTie(player, opponent);
			opponent = players[(player.getPlayerID() + 1)
					% Constants.PLAYER_COUNT]; // What does this bit of logic do? (Is this server supposed to run multiple 2-player games simultaneously? Otherwise, this makes no sense to me.) - Ace

			playerTurn.setWon();
			Server.serverLog.log(Level.INFO, "Player " + playerTurn.getPlayer().getName() + " has won! (Won a tie breaker)");
			opponentPlayerTurn.setLost();
			winner = true;
		}

		opponentPlayerTurn.setNotify();
		turn++;
		return true;
	}

	/**
	 * Get if the game has started. This will probably be called in the context
	 * of checking if there are enough players to start the game
	 * 
	 * @return if the game has started
	 */
	public boolean getStarted() {
		return started;
	}
	
	/**
	 * Get if the game has a winner.
	 * @return if the game has a winner
	 */
	public boolean getWinner() {
		return winner;
	}

	/**
	 * Get the current turn
	 * 
	 * @return the current turn
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * Gets the ID of the player of the current turn
	 * 
	 * @return
	 */
	public int getCurrPlayerID() {
		return turn % Constants.PLAYER_COUNT;
	}

	/**
	 * Gets the ID of the opponent of the current turn
	 */
	public int getCurrOpponentID() {
		return (getCurrPlayerID() + 1) % Constants.PLAYER_COUNT;
	}

	/**
	 * Notifies the engine that a player lost his turn or was interrupted
	 * otherwise
	 */
	public void notifyInterrupt() {
		turn++;
	}
}
