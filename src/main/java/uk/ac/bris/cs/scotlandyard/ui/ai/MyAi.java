package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;


import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "Name me!"; }
	private Integer getScore(Board board)	{
		ArrayList<Move> moves = new ArrayList<>(board.getAvailableMoves().asList());
		Move returnMove;
		for(Move move : moves)	{
			//TODO look at the destination and see if there are any detectives surrounding this node.
			Integer destination = move.visit(new Move.Visitor<Integer>(){
				public Integer visit(Move.SingleMove singleMove){
					return singleMove.destination;
				}
				public Integer visit(Move.DoubleMove doubleMove){
					return doubleMove.destination2;
				}
			});
			for(Piece detective : board.getPlayers().asList())	{
				//if detective location is one away from moving to destination don't add it.
			}
			//TODO count number of possible moves from this destination
			//TODO calculate a score for this: 1 point for each available move, 0 if there is a detective within striking distance.
			//advanced: implement dijkstra's algorithm and scale deducting points based on distance detectives are away.
			//advanced: base it on tickets detectives have left and their available moves too.
		}
		return null;
	}

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		// returns a random move, replace with your own implementation
		var moves = board.getAvailableMoves().asList();
		return moves.get(new Random().nextInt(moves.size()));
	}
}
