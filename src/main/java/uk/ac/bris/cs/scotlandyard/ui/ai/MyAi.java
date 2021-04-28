package uk.ac.bris.cs.scotlandyard.ui.ai;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.MrX;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MyAi implements Ai {
	//returns all moves that don't lead to losing on the next turn from current location.
	private ArrayList<Move> getGoodMoves(Board board)	{
		ArrayList<Move> goodMoves = new ArrayList<Move>(board.getAvailableMoves());
		ArrayList<Piece> pieces = new ArrayList<>(board.getPlayers());
		boolean badMove = false;
		ArrayList<Piece.Detective> detectives = new ArrayList<Piece.Detective>();
		for(Piece piece : pieces)	{
			if(piece.isDetective())	{
				detectives.add((Piece.Detective) piece);
			}
		}
		for(Move move : board.getAvailableMoves()) {
			Integer destination = move.visit(new Move.Visitor<Integer>() {
				public Integer visit(Move.SingleMove singleMove) {
					return singleMove.destination;
				}
				public Integer visit(Move.DoubleMove doubleMove) {
					return doubleMove.destination2;
				}
			});
			ArrayList<Integer> adjNodes = new ArrayList<Integer>(board.getSetup().graph.adjacentNodes(destination));
			for(Piece.Detective detective : detectives)	{
				Optional<Integer> detectiveLoc = board.getDetectiveLocation(detective);
				for(Integer node : adjNodes)	{
					if(detectiveLoc.get().equals(node))	{
						badMove = true;
					}
				}
			}
			if(badMove)	{
				goodMoves.remove(move);
				badMove = false;
			}
			System.out.println(board.getPlayers());
		}
		return goodMoves;
	}
	private Integer countMoves(Board board)	{
		return getGoodMoves(board).size();
	}
	//count number of moves available after moving and rank them
	private ArrayList<Move> getNextMoves(Board board, ArrayList<Move> moves)	{
		for(Move move : moves)	{
			Integer destination = move.visit(new Move.Visitor<Integer>() {
				public Integer visit(Move.SingleMove singleMove) {
					return singleMove.destination;
				}
				public Integer visit(Move.DoubleMove doubleMove) {
					return doubleMove.destination2;
				}
			});
			//count number of available moves from this destination.


		}
		return moves;
	}
	@Nonnull @Override public String name() { return "Caesar"; }
	private Integer getScore(Board board)	{
		//DONE look at the destination and see if there are any detectives surrounding this node.

		// DONE if detective location is one away from moving to destination don't add it.

		//TODO count number of possible moves from this destination
		Integer numMoves = countMoves(board);
		//TODO calculate a score for this: 1 point for each available move, 0 if there is a detective within striking distance.
		//advanced: implement dijkstra's algorithm and scale deducting points based on distance detectives are away.
		//advanced: base it on tickets detectives have left and their available moves too.

		return countMoves(board);
	}

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		var moves = getGoodMoves(board);
		return moves.get(new Random().nextInt(moves.size()));
	}
}
