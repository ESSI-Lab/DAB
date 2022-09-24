package eu.essi_lab.userbase.api;

import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.userbase.exception.InvalidUser;

/**
 * Api provides tools to interact with an underlying persistence engine to
 * C(reate)R(ead)U(pdate)D(elete) entities who belong the
 * authentication/authorization datamodel context.
 * 
 * @author pezzati
 */
public interface UserbaseAPI {

    /**
     * The method store the given {@link GSUser} or throws a {@link GSException}
     * if the underlying persistence engine raises some exceptions.
     * 
     * @param user the user to be stored.
     * @throws GSException common wrapper about all the checked or documented
     *         exceptions the persistence engine can raise.
     */
    void createUser(GSUser user) throws GSException;

    /**
     * Validate a {@link GSUser} instance. If instance isn't valid, method
     * raises an {@link GSException} exception.
     * 
     * @param user the {@link GSUser} to validate.
     * @throws InvalidUser if {@link GSUser} instance isn't valid.
     */
    void validateUser(GSUser user) throws GSException;

    /**
     * Given a username method returns corresponding {@link GSUser} entity
     * stored in database. User's username is assumed to be unique in database.
     * 
     * @param username a string representing the user's name. We assume username
     *        to be unique.
     * @return a {@link GSUser} object who implements the corresponding user.
     * @throws GSException common wrapper about all the checked or documented
     *         exceptions the persistence engine can raise.
     */
    GSUser readUser(String username) throws GSException;

    /**
     * Delete the user corresponding the given username. We assume username to
     * be unique in database.
     * 
     * @param username name of the user we want to remove from database.
     * @throws GSException common wrapper about all the checked or documented
     *         exceptions the persistence engine can raise.
     */
    void deleteUser(String username) throws GSException;

    /**
     * Given a valid user, method retreives corresponding user into database,
     * update it and return a reference to it.
     * 
     * @param user the instance of {@link GSUser} who will replace the old one
     *        into database (if {@link GSUser#getEmail()} values match.
     * @return reference to the updated user.
     * @throws GSException common wrapper about all the checked or documented
     *         exceptions the persistence engine can raise.
     */
    GSUser updateUser(GSUser user) throws GSException;

    /**
     * Given a valid user, method retreives the corresponding user into database
     * and, if found, method updates it as enabled.
     * 
     * @param user the user we want to update as enabled.
     * @throws GSException common wrapper about all the checked or documented
     *         exceptions the persistence engine can raise.
     */
    void enable(GSUser user) throws GSException;

    /**
     * Given a valid user, method retreives the corresponding user into database
     * and, if found, method updates it as disabled.
     * 
     * @param user the user we want to update as disabled.
     * @throws GSException common wrapper about all the checked or documented
     *         exceptions the persistence engine can raise.
     */
    void disable(GSUser user) throws GSException;
}
