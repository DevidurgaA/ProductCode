package com.tlc.sql.api.dml;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Abishek
 * @version 1.0
 */
public class WhereClause implements Serializable
{
	public enum Connector
	{
		AND, OR
	}
	
	private final WhereClause left;
	private final WhereClause right;
	
	private final Connector connector;

	private final boolean isConnector;
	private final boolean isInverted;
	
	private final Criteria criteria;
	public WhereClause(Criteria criteria)
	{
		this.isConnector = false;
		this.isInverted = false;
		this.connector = null;
		this.left = null;
		this.right = null;
		this.criteria = Objects.requireNonNull(criteria);
	}

	public WhereClause(WhereClause criteriaBuilder, boolean isInverted)
	{
		this.isConnector = criteriaBuilder.isConnector;
		this.isInverted = isInverted;
		this.connector = criteriaBuilder.connector;
		this.left = criteriaBuilder.left;
		this.right = criteriaBuilder.right;
		this.criteria = criteriaBuilder.criteria;
	}

	private WhereClause(WhereClause left, WhereClause right, Connector connector)
	{
		this.isConnector = true;
		this.isInverted = false;
		this.left = Objects.requireNonNull(left);
		this.right = Objects.requireNonNull(right);
		this.connector = connector;
		this.criteria = null;
	}

	public WhereClause or(Criteria criteria)
	{
		return new WhereClause(this, new WhereClause(criteria), Connector.OR);
	}

	public WhereClause or(WhereClause tree)
	{
		return new WhereClause(this, Objects.requireNonNull(tree), Connector.OR);
	}

	public WhereClause and(WhereClause tree)
	{
		return new WhereClause(this, Objects.requireNonNull(tree), Connector.AND);
	}

	public WhereClause and(Criteria criteria)
	{
		return new WhereClause(this, new WhereClause(criteria), Connector.AND);
	}

	public WhereClause not()
	{
		return new WhereClause(this, !this.isInverted());
	}
	
	public WhereClause getLeft()
	{
		return left;
	}

	public WhereClause getRight()
	{
		return right;
	}
	
	public Connector getConnector()
	{
		return connector;
	}

	public boolean isConnector()
	{
		return isConnector;
	}
	
	public boolean isInverted()
	{
		return isInverted;
	}

	public Criteria getCriteria()
	{
		return criteria;
	}

	public WhereClause copy()
	{
		return copy(this);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		loadCriteriaString(builder, this);
		return builder.toString();
	}

	private WhereClause copy(WhereClause criteriaTree)
	{
		final WhereClause newBuilder;
		if(criteriaTree.isConnector)
		{
			final WhereClause left = copy(Objects.requireNonNull(criteriaTree.left));
			final WhereClause right = copy(Objects.requireNonNull(criteriaTree.right));
			newBuilder = new WhereClause(left, right, criteriaTree.connector);
		}
		else
		{
			newBuilder = new WhereClause(criteriaTree.criteria);
		}
		return criteriaTree.isInverted ? newBuilder.not() : newBuilder;
	}

	private void loadCriteriaString(StringBuilder builder, WhereClause criteriaBuilder)
	{
		if(criteriaBuilder.isInverted())
		{
			builder.append("!");
		}
		if(criteriaBuilder.isConnector())
		{
			builder.append("(");

			final WhereClause left = criteriaBuilder.getLeft();
			final WhereClause right = criteriaBuilder.getRight();
			
			loadCriteriaString(builder, left);
			
			if(criteriaBuilder.getConnector() == Connector.AND)
			{
				builder.append(" && ");
			}
			else
			{
				builder.append(" || ");
			}

			loadCriteriaString(builder, right);

			builder.append(")");
		}
		else
		{
			builder.append(criteriaBuilder.getCriteria());
		}
	}
}
