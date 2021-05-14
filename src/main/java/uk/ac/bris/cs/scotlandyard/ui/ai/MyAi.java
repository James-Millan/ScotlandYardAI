package uk.ac.bris.cs.scotlandyard.ui.ai;
import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MyAi implements Ai {

    private ArrayList<Piece.Detective> getDetectives(Board board)	{
		ArrayList<Piece.Detective> detectives = new ArrayList<>();
		ArrayList<Piece> pieces = new ArrayList<>(board.getPlayers());
		pieces.removeIf(Piece::isMrX);
		for (Piece piece : pieces)	{
			if(piece.isDetective())	{
				detectives.add((Piece.Detective) piece);
			}
		}
		return detectives;
	}
	//returns all moves that don't lead to losing on the next turn from current location.
	private ArrayList<Move> getGoodMoves(Board board)	{
		ArrayList<Move> goodMoves = new ArrayList<>(board.getAvailableMoves());
		boolean badMove = false;
		ArrayList<Piece.Detective> detectives = new ArrayList<>(getDetectives(board));
		for(Move move : board.getAvailableMoves()) {
			Integer destination = move.visit(new Move.Visitor<>() {
				public Integer visit(Move.SingleMove singleMove) {
					return singleMove.destination;
				}
				public Integer visit(Move.DoubleMove doubleMove) {
					return doubleMove.destination2;
				}
			});
			ArrayList<Integer> adjNodes = new ArrayList<>(board.getSetup().graph.adjacentNodes(destination));
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
	private ImmutableSet<Move.SingleMove> makeSingleMoves(
			GameSetup setup,
			ArrayList<Piece.Detective> detectives,
			Piece player,
			int source,
			Board board){
		final var singleMoves = new ArrayList<Move.SingleMove>();
		for(int destination : setup.graph.adjacentNodes(source)) {
			for(Piece.Detective detective: detectives) {
				if (!(destination == board.getDetectiveLocation(detective).get()) ){
					for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source,destination,ImmutableSet.of())) {
						if (board.getPlayerTickets(player).get().getCount(t.requiredTicket()) > 0) {
							Move.SingleMove moveToAdd = new Move.SingleMove(player, source, t.requiredTicket(), destination);
							singleMoves.add(moveToAdd);
						}
					}
					if(board.getPlayerTickets(player).get().getCount(ScotlandYard.Ticket.SECRET) > 0){
						Move.SingleMove moveToAdd = new Move.SingleMove(player, source, ScotlandYard.Ticket.SECRET, destination);
						singleMoves.add(moveToAdd);
					}
				}
			}
		}
		return ImmutableSet.copyOf(singleMoves);
	}
	private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
			GameSetup setup,
			ArrayList<Piece.Detective> detectives,
			Piece player,
			int source,
			Board board){
		ArrayList<Move.DoubleMove> doubleMoves = new ArrayList<Move.DoubleMove>();
		// For all adjacent locations of player
		for(int destination : setup.graph.adjacentNodes(source)) {
			for(Piece.Detective detective: detectives) {
				// Check whether a detective is in location and whether player has a double ticket
				if (!(destination == board.getDetectiveLocation(detective).get()) && board.getPlayerTickets(player).get().getCount(ScotlandYard.Ticket.DOUBLE) > 0){
					// Check if player has ticket for move 1
					for(ScotlandYard.Transport t1 : setup.graph.edgeValueOrDefault(source,destination,ImmutableSet.of())){
						if (board.getPlayerTickets(player).get().getCount(t1.requiredTicket()) > 0) {
							// For all adjacent locations of 1st move location
							for(int destination2 : setup.graph.adjacentNodes(destination)) {
								for(Piece.Detective detective2: detectives){
									// Check whether a detective is in location
									if (!(destination2 == board.getDetectiveLocation(detective2).get())) {
										// Check if player has ticket for move 2
										for(ScotlandYard.Transport t2 : setup.graph.edgeValueOrDefault(destination,destination2,ImmutableSet.of())) {
											if (board.getPlayerTickets(player).get().getCount(t2.requiredTicket()) > 0) {
												//check that player has enough tickets
												Integer value = board.getPlayerTickets(player).get().getCount(t1.requiredTicket());
												if(!((t1.equals(t2)) && value.equals(1))) {
													Move.DoubleMove moveToAdd = new Move.DoubleMove(player, source, t1.requiredTicket(),
															destination, t2.requiredTicket(), destination2);
													doubleMoves.add(moveToAdd);
												}
												if (board.getPlayerTickets(player).get().getCount(ScotlandYard.Ticket.SECRET) > 0) {
													Move.DoubleMove moveToAdd = new Move.DoubleMove(player, source, t1.requiredTicket(),
															destination, ScotlandYard.Ticket.SECRET, destination2);
													doubleMoves.add(moveToAdd);
												}
											}

										}
									}
								}
							}
						}
						if (board.getPlayerTickets(player).get().getCount(ScotlandYard.Ticket.SECRET) > 0)	{
							for(int destination2 : setup.graph.adjacentNodes(destination)) {
								for(Piece.Detective detective2: detectives){
									// Check whether a detective is in location
									if (!(destination2 == board.getDetectiveLocation(detective2).get())) {
										// Check if player has ticket for move 2
										for(ScotlandYard.Transport t2 : setup.graph.edgeValueOrDefault(destination,destination2,ImmutableSet.of())) {
											if (board.getPlayerTickets(player).get().getCount(t2.requiredTicket()) > 0) {
												Move.DoubleMove moveToAdd = new Move.DoubleMove(player, source, ScotlandYard.Ticket.SECRET,
														destination, t2.requiredTicket(), destination2);
												doubleMoves.add(moveToAdd);

											}
											if ((board.getPlayerTickets(player).get().getCount(ScotlandYard.Ticket.SECRET) > 1))	{

												Move.DoubleMove moveToAdd = new Move.DoubleMove(player, source, ScotlandYard.Ticket.SECRET,
														destination, ScotlandYard.Ticket.SECRET, destination2);
												doubleMoves.add(moveToAdd);

											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ImmutableSet.copyOf(doubleMoves);
	}
	private ImmutableSet<Move> getAvailableMoves(Board board, Piece player, Integer source) {
		ArrayList<Move> tempMoves = new ArrayList<>(makeSingleMoves(board.getSetup(), getDetectives(board), player, source, board));
		if (player.isMrX() && board.getSetup().rounds.size() > 1) {
			tempMoves.addAll(makeDoubleMoves(board.getSetup(), getDetectives(board), player, source, board));
		}
		return ImmutableSet.copyOf(tempMoves);
	}

	private Integer getDestination(Move move)	{
		return move.visit(new Move.Visitor<>(){
			public Integer visit(Move.SingleMove singleMove){
				return singleMove.destination;
			}
			public Integer visit(Move.DoubleMove doubleMove){
				return doubleMove.destination2;
			}
		});
	}
	@Nonnull @Override public String name() { return "Caesar"; }
	private Move getBestMove(Board board)	{
		ArrayList<Move> moves = getGoodMoves(board);
		HashMap<Integer, Move> rankedMoves = new HashMap<>();
		int maxScore = 0;
		for(Move move : moves)	{
			Integer destination = getDestination(move); // visitor to get destination
			int score = getAvailableMoves(board, move.commencedBy(), destination).size();
			rankedMoves.put(score, move);
			if(score > maxScore)	{
				maxScore = score;
			}
		}
		return rankedMoves.get(maxScore);
	}
	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		var moves = getGoodMoves(board);
		if(!moves.isEmpty()) {
			return getBestMove(board);
		}
		else  {
			//can return a random one since will lose on next turn anyway. If detectives play properly.
			ArrayList<Move> badMoves = new ArrayList<>(board.getAvailableMoves());
			return badMoves.get(new Random().nextInt(badMoves.size()));
		}
	}
}
