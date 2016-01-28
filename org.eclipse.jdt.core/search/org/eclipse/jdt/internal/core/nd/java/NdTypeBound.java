package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Represents the bound on a generic parameter (a ClassBound or InterfaceBound in
 * the sense of the Java VM spec Java SE 8 Edition, section 4.7.9.1)
 *
 * @since 3.12
 */
public class NdTypeBound extends NdNode {
	public static final FieldManyToOne<NdTypeParameter> PARENT;
	public static final FieldManyToOne<NdTypeSignature> TYPE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdTypeBound> type;

	static {
		type = StructDef.create(NdTypeBound.class, NdNode.type);
		PARENT = FieldManyToOne.createOwner(type, NdTypeParameter.BOUNDS);
		TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_TYPE_BOUND);

		type.done();
	}

	public NdTypeBound(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdTypeBound(NdTypeParameter parent, NdTypeSignature signature) {
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
		TYPE.put(getNd(), this.address, signature);
	}

	public NdTypeParameter getParent() {
		return PARENT.get(getNd(), this.address);
	}

	public NdTypeSignature getType() {
		return TYPE.get(getNd(), this.address);
	}
}