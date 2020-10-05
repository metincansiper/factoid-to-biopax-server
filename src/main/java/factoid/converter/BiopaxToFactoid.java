package factoid.converter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.paxtools.model.level3.Xref;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



public class BiopaxToFactoid {
	
	public BiopaxToFactoid() {
		
	}
	
	public JsonObject convert(Model model) {
		JsonObject o = new JsonObject();
		Set<Interaction> intns = model.getObjects(Interaction.class);
//		for ( Interaction intn : intns ) {
//			System.out.println(intn);
//		}
		intns = intns.stream()
				.filter(intn -> intn.getControlledOf().size() == 0)
				.filter(intn -> keepIntn(intn))
				.collect(Collectors.toSet());

		for (Interaction intn : intns) {
			Set<String> markedPmids = new HashSet<String>();
			Process controlled = getControlled(intn);
			for ( Evidence evidence : controlled.getEvidence() ) {
				for (Xref xref : evidence.getXref()) {
					String db = xref.getDb();
					if ( db != null && db.equalsIgnoreCase("pubmed") ) {
						String pmid = xref.getId();
						System.out.println(pmid);
						if ( pmid != null && !markedPmids.contains(pmid) ) {
							System.out.println(intn.getClass());
							System.out.println(intn.getControlledOf());
							if (o.get(pmid) == null) {
								o.add(pmid, new JsonArray());
							}
							JsonArray arr = (JsonArray) o.get(pmid);
							arr.add(intnToJson(intn));
							markedPmids.add(pmid);
							System.out.println(intn.getUri() + ' ' + pmid);
						}
					}
				}
			}
		}
		return o;
	}
	
	private <T extends Object>  T getOptional(Optional<T> o) {
		if ( o.isPresent() ) {
			return o.get();
		}
		
		return null;
	}
	
	private Process getControlled(Interaction intn) {
		Set<Process> s = null;
		if ( intn instanceof Control ) {
			s = ((Control) intn).getControlled();
		}
		if ( intn instanceof TemplateReactionRegulation ) {
			s = ((TemplateReactionRegulation) intn).getControlled();
		}
		if ( intn instanceof Catalysis ) {
			s = ((Catalysis) intn).getControlled();
		}
		if ( s == null ) {
			return null;
		}
		Optional<Process> first = s.stream().findFirst();
		if ( first.isPresent() ) {
			return first.get();
		}
		
		return null;
	}
	
	private boolean keepIntn(Interaction intn) {
		Process controlled = getControlled(intn);
		if ( controlled == null ) {
			return false;
		}
		for( Evidence evidence : controlled.getEvidence() ) {
			for(EvidenceCodeVocabulary v : evidence.getEvidenceCode()) {
				for(Xref xref : v.getXref()) {
					String id = xref.getId();
					System.out.println("keep intn check");
					if ( id.equalsIgnoreCase("MI:0074") || id.equalsIgnoreCase("MI:0421")
							|| id.equalsIgnoreCase("MI:0113")) {
						System.out.println("keep intn");
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private JsonObject makeIntnJson(String type, ControlType ctrlType, Set<Entity> participants, Entity src, Entity tgt) {
		JsonObject obj = new JsonObject();
		obj.addProperty("type", type);
		
		if ( ctrlType != null ) {
			obj.addProperty("controlType", ctrlType.toString());
		}
		
		if (participants != null) {
			Set<JsonObject> participantObjs = participants.stream().map(e -> makeEntityJson(e)).collect(Collectors.toSet());
			JsonArray participantsArr = new JsonArray();
			for ( JsonObject o : participantObjs ) {
				participantsArr.add(o);
			}
			
			obj.add("participants", participantsArr);
		}
		
		if (src != null && tgt != null) {
			obj.add("controller", makeEntityJson(src));
			obj.add("target", makeEntityJson(tgt));
			
			if ( tgt instanceof PhysicalEntity ) {
				EntityFeature mf = getOptional(((PhysicalEntity) tgt).getFeature().stream().filter(f -> f instanceof ModificationFeature).findFirst());
				if ( mf != null ) {
					obj.addProperty("modification", mf.toString());
				}
			}
			
		}

		return obj;
	}
	
	private JsonObject makeEntityJson(Entity entity) {
		String type = getEntityType(entity);
		String name = entity.getDisplayName();
		JsonObject xref = xrefToJson(getOptional(entity.getXref().stream().findFirst()));
		
		JsonObject obj = new JsonObject();
		obj.addProperty("type", type);
		obj.addProperty("name", name);
		obj.add("xref", xref);
		
		return obj;
	}
	
	private JsonObject xrefToJson(Xref xref) {
		JsonObject xrefJson = null;
		
		if ( xref != null ) {
			xrefJson = new JsonObject();
			xrefJson.addProperty("id", xref.getId());
			xrefJson.addProperty("db", xref.getDb());
		}
		
		return xrefJson;
	}
	
	private String getEntityType(Entity e) {
		if ( e instanceof Protein ) {
			return "protein";
		}
		else if ( e instanceof SmallMolecule ) {
			return "chemical";
		}
		else if ( e instanceof Complex ) {
			return "complex";
		}
		else if ( e instanceof DnaRegion ) {
			return "dna";
		}
		else if ( e instanceof Rna ) {
			return "rna";
		}
		else {
			try {
				throw new Exception("Unexpected enitity class" + e.getClass().toString());
			} catch (Exception exc) {
				exc.printStackTrace();
				return null;
			}
		}
	}
	
	private boolean isMacromolecule(Class c) {
		return isDna(c) || isRna(c) || isProtein(c);
	}
	
	private boolean isDna(Class c) {
		return c == Dna.class;
	}
	
	private boolean isRna(Class c) {
		return c == Rna.class;
	}
	
	private boolean isProtein(Class c) {
		return c == Protein.class;
	}
	
	private boolean isComplex(Class c) {
		return c == Complex.class;
	}
	
	private boolean isChemical(Class c) {
		return c == SmallMolecule.class;
	}
	
	private PhysicalEntity getLeafEntity(Entity entity) {
		if (entity instanceof PhysicalEntity) {
			return (PhysicalEntity) entity;
		}
		if (entity instanceof Interaction) {
			Set<Entity> ppts = ((Interaction) entity).getParticipant();
			for ( Entity ppt : ppts ) {
				PhysicalEntity leaf = getLeafEntity(ppt);
				if ( leaf != null ) {
					return leaf;
				}
			}
		}
		return null;
	}
	
	private JsonObject intnToJson(Interaction intn) {
		Class c = intn.getClass();
		
//		if ( c == MolecularInteraction.class ) {
//			JsonObject obj = makeIntnJson("Molecular Interaction", null, intn.getParticipant(), null, null);
//			return obj;
//		}
		if ( c == TemplateReactionRegulation.class ) {
			TemplateReactionRegulation regulation = (TemplateReactionRegulation) intn;
			Controller src = getOptional(regulation.getController().stream().findFirst());
			Class srcClass = src.getClass();
			if ( isMacromolecule(srcClass) || isComplex(srcClass) ) {
				Process reaction = getOptional(regulation.getControlled().stream().findFirst());
				if (reaction.getClass() == Process.class) {
					PhysicalEntity tgt = getOptional(((TemplateReaction) reaction).getProduct().stream().findFirst());
					Class tgtClass = tgt.getClass();
					if ( isProtein(tgtClass) || isRna(tgtClass) ) {
						ControlType ctrlType = regulation.getControlType();
						JsonObject obj = makeIntnJson("Expression Regulation", ctrlType, null, src, tgt);
						return obj;
					}
				}
			}
		}
		if ( c == Catalysis.class ) {
			Catalysis catalysis = (Catalysis) intn;
			Controller src = getOptional(catalysis.getController().stream().findFirst());
			Class srcClass = src.getClass();
			if ( isMacromolecule(srcClass) || isComplex(srcClass) ) {
				Conversion conversion = (Conversion) getOptional(catalysis.getControlled().stream().findFirst());
				// getLeft() would also work instead of getRight()				
				PhysicalEntity tgt = getOptional(((Conversion) conversion).getRight().stream().findFirst());
				Class tgtClass = tgt.getClass();
				if ( isMacromolecule(tgtClass) ) {
					ControlType ctrlType = catalysis.getControlType();
					JsonObject obj = makeIntnJson("Protein Controls State", ctrlType, null, src, tgt);
					return obj;
				}
			}
		}
		
		Set<Entity> ppts = intn.getParticipant();
		List<Entity> pePPts = ppts.stream().filter(p -> p instanceof PhysicalEntity).collect(Collectors.toList());
		List<Entity> intnPPts = ppts.stream().filter(p -> p instanceof Interaction).collect(Collectors.toList());
		if ( pePPts.size() == ppts.size() ) {
			JsonObject obj = makeIntnJson("Other", null, ppts, null, null);
			return obj;
		}
		if ( pePPts.size() == 1 && intnPPts.size() == 1 ) {
			PhysicalEntity src = (PhysicalEntity) pePPts.get(0);
			PhysicalEntity tgt = getLeafEntity(intnPPts.get(0));
			
			if ( tgt != null ) {
				JsonObject obj = makeIntnJson("Other", null, null, src, tgt);
				return obj;
			}
		}
		Set<Entity> leafs = ppts.stream()
				.map(p -> getLeafEntity(p))
				.filter(l -> l != null)
				.collect(Collectors.toSet());
		
		JsonObject obj = makeIntnJson("Other", null, leafs, null, null);
		return obj;
	}
}
