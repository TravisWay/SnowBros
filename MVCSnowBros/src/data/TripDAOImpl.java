package data;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import entities.Destination;
import entities.Message;
import entities.Trip;
import entities.User;

@Transactional
@Repository
public class TripDAOImpl implements TripDAO {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Trip createTrip(Trip trip) {
		try {
			em.persist(trip);

		} catch (Exception e) {
			return null;
		}
		em.flush();

		return trip;
	}

	@Override
	public Set<Trip> searchTrip(String search) {
		Set<Trip> trips = new HashSet<>();
		String query1 = "Select t FROM Trip t Where t.title LIKE :search";

																							// thing

		String query2 = "SELECT u FROM User u JOIN FETCH u.trips WHERE u.firstName LIKE :search OR u.lastName LIKE :search1"; // set
																													// 2
																													// parameters
																													// to
																													// the
																													// same
																													// thing

		// String query3 = "SELECT ec.trips FROM ExtraCurr ec WHERE ec.name LIKE
		// :search";
		String query4 = "SELECT d FROM Destination d WHERE d.name LIKE :search";

		try {
			List<Trip> titleTrips = em.createQuery(query1, Trip.class).setParameter("search", "%" + search + "%")
					.getResultList();
			for (Trip trip : titleTrips) {
				trips.add(trip);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(trips);
		try {

			List<User> users = em.createQuery(query2, User.class).setParameter("search", "%" + search + "%")
					.setParameter("search1", "%" + search + "%").getResultList();
			List<Trip> nameTrips = users.get(0).getTrips();
			for (Trip trip : nameTrips) {
				trips.add(trip);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(trips);
//		// try {
//		// List<Trip> ecTrips = em.createQuery(query3,
//		// Trip.class).setParameter("search", "%" + search + "%")
//		// .getResultList();
//		// for (Trip trip : ecTrips) {
//		// trips.add(trip);
//		// }
//		// } catch (Exception e) {
//		// e.printStackTrace();
//		// }
//
		try {
			List<Trip> destTrips = em.createQuery(query4, Destination.class).setParameter("search", "%" + search + "%")
					.getResultList().get(0).getTrips();
			for (Trip trip : destTrips) {
				trips.add(trip);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(trips);
		return trips;
	}

	@Override
	public Trip updateTrip(Trip trip) {
		Trip tripupdated = em.find(Trip.class, trip.getId());
		// update object
		tripupdated.setDate(trip.getDate());
		tripupdated.setDescription(trip.getDescription());
		tripupdated.setDestination(trip.getDestination());
		tripupdated.setExtraCurrs(trip.getExtraCurrs());
		tripupdated.setNumberSeats(trip.getNumberSeats());
		tripupdated.setPointOfOrigin(trip.getPointOfOrigin());
		tripupdated.setPointOfReturn(trip.getPointOfReturn());
		tripupdated.setRoundtrip(trip.getRoundtrip());
		tripupdated.setTitle(trip.getTitle());
		tripupdated.setUsers(trip.getUsers());

		return tripupdated;

	}

	@Override
	public Trip deleteTrip(Trip trip) {
		Trip trip1 = null;
		try {
			trip1 = em.find(Trip.class, trip.getId());
			em.remove(trip1);

		} catch (Exception e) {
			e.printStackTrace();
			trip1 = null;
		}

		return trip1;

	}

	@Override
	public List<Trip> allTrips() {
		return em.createQuery("Select t FROM Trip t", Trip.class).getResultList();

	}

	@Override
	public Trip findTripById(int id) {
		Trip t = em.find(Trip.class, id);
		return t;
	}

	@Override
	public Destination findDestinationByNameOrCreateNewDestination(String name) {
		String query = "SELECT d FROM Destination d WHERE name = :name";
		Destination d = new Destination();
		try {
			d = em.createQuery(query, Destination.class).setParameter("name", name).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (d.getName() != null) {
			return d;
		} else {
			d.setName(name);
			em.persist(d);
			em.flush();
			return d;
		}
	}
	
	@Override
	public Trip removeBroFromTrip(Trip trip, User bro) {
		List<User> usersOnTrip = trip.getUsers();
		for (User user : usersOnTrip) {
			if (user.getId() == bro.getId()) {
				usersOnTrip.remove(user);
				trip.setNumberSeats(trip.getNumberSeats() + 1);
				List<Trip> brosTrips = bro.getTrips();
				for (Trip broTrip : brosTrips) {
					if (broTrip.getId() == trip.getId()) {
						brosTrips.remove(broTrip);
						bro.setTrips(brosTrips);
						UserDAOImpl ud = new UserDAOImpl();
						ud.updateUser(bro);
						break;
					}
				}
				break;
			}
		}
		trip.setUsers(usersOnTrip);
		return trip;
	}
	
	@Override
	public List<User> getAllUsersOnTrip(int tripId) {
		List<User> usersOnTrip = new ArrayList<>();
		String query = "SELECT t FROM Trip t JOIN FETCH t.users WHERE t.id = :id";
		try {
			Trip thisTrip = em.createQuery(query, Trip.class).setParameter("id", tripId)
						  .getResultList().get(0);
			usersOnTrip = thisTrip.getUsers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return usersOnTrip;
	}
	
	@Override
	public List<Message> getMessagesByTripId(int tripId) {
		List<Message> messages = new ArrayList<>();
		String query = "SELECT t FROM Trip t JOIN FETCH t.messages WHERE t.id = :id";
		try {
			Trip thisTrip = em.createQuery(query, Trip.class)
						      .setParameter("id", tripId)
						      .getResultList().get(0);
			messages = thisTrip.getMessages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messages;
	}
	
	@Override
	public Trip addMessage(User user, Trip trip, String message, Date date) {
		Message m = new Message();
		m.setOwnerName(user.getFirstName() + " " + user.getLastName());
		m.setMessage(message);
		m.setTrip(trip);
		String datey = date.toString();
		
		System.out.println(datey);
		final String regex = "[\\w\\d]{3,5}\\s[\\w\\d]{3,5}\\s[\\d]{2}";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(datey);
		while (matcher.find()) {
			datey = matcher.group(); 
			
		}
		m.setDate(datey);
		
		em.persist(m);
		em.flush();
		
		List<Message> messages = new ArrayList<>();
		String query = "SELECT t FROM Trip t JOIN FETCH t.messages WHERE t.id = :id";
		try {
			Trip thisTrip = em.createQuery(query, Trip.class)
						      .setParameter("id", trip.getId())
						      .getResultList().get(0);
			messages = thisTrip.getMessages();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		messages.add(m);
		trip.setMessages(messages);
		
		return trip;
	}

}
