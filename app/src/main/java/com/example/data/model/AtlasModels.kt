package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class SystemId(
    val id: String,
    val displayName: String,
    val swatchColor: Color,
    val description: String,
) {
    SKELETAL(
        id = "skeletal",
        displayName = "Skeleton",
        swatchColor = Color(0xFFE2D9BA),
        description = "Bones form the supporting framework of the body, protect organs, and provide attachment points for muscles. Their internal tissue also stores minerals and produces blood cells.",
    ),
    MUSCULAR(
        id = "muscular",
        displayName = "Muscles",
        swatchColor = Color(0xFFA85B50),
        description = "Skeletal muscles generate movement by pulling on their attachments. Together with tendons, they move joints, stabilize posture, and produce heat.",
    ),
    CARDIAC(
        id = "cardiac",
        displayName = "Heart",
        swatchColor = Color(0xFFB96760),
        description = "The heart is a muscular pump with four chambers. Its valves direct blood forward through the pulmonary and systemic circuits.",
    ),
    SENSORY(
        id = "sensory",
        displayName = "Sensory organs",
        swatchColor = Color(0xFFB0C8CE),
        description = "These structures contribute to special senses, including sight, hearing, and balance. Their specialized tissues detect stimuli and work with the nervous system to convey information.",
    ),
    ARTERIAL(
        id = "arterial",
        displayName = "Arteries",
        swatchColor = Color(0xFFC05245),
        description = "The heart drives blood through the circulation. Arteries carry blood away from the heart to supply tissues or, in the pulmonary circuit, to the lungs.",
    ),
    VENOUS(
        id = "venous",
        displayName = "Veins",
        swatchColor = Color(0xFF527C9F),
        description = "Veins return blood toward the heart. Superficial and deep networks collect blood from the tissues; the pulmonary veins bring oxygenated blood back from the lungs.",
    ),
    NERVOUS(
        id = "nervous",
        displayName = "Nervous system",
        swatchColor = Color(0xFFD8B565),
        description = "The brain, spinal cord, and peripheral nerves carry and process signals. They support sensation, movement, coordination, and automatic regulation of body functions.",
    ),
    RESPIRATORY(
        id = "respiratory",
        displayName = "Respiratory",
        swatchColor = Color(0xFFB98991),
        description = "The airways conduct air to the lungs, where oxygen and carbon dioxide move between air and blood. Breathing depends on pressure changes produced by respiratory muscles.",
    ),
    DIGESTIVE(
        id = "digestive",
        displayName = "Digestive",
        swatchColor = Color(0xFFB8916B),
        description = "The digestive tract breaks down food, absorbs nutrients and water, and moves waste onward. Accessory organs contribute bile and digestive enzymes.",
    ),
    URINARY(
        id = "urinary",
        displayName = "Urinary",
        swatchColor = Color(0xFFB47961),
        description = "The kidneys filter blood and regulate fluid, electrolyte, and acid-base balance. Urine travels through the ureters to the bladder and exits through the urethra.",
    ),
    LYMPHATIC(
        id = "lymphatic",
        displayName = "Lymphatic",
        swatchColor = Color(0xFF879F7C),
        description = "Lymphatic vessels return excess tissue fluid to the circulation. Lymph nodes and other lymphoid organs support immune surveillance and responses.",
    ),
    ENDOCRINE(
        id = "endocrine",
        displayName = "Endocrine",
        swatchColor = Color(0xFFC5A09A),
        description = "Endocrine organs release hormones into the blood to coordinate processes such as metabolism, growth, stress responses, and reproduction.",
    ),
    REPRODUCTIVE(
        id = "reproductive",
        displayName = "Reproductive",
        swatchColor = Color(0xFFBDA098),
        description = "The male reproductive structures represented here contribute to sperm production, maturation, transport, and the production of sex hormones.",
    ),
    INTEGUMENTARY(
        id = "integumentary",
        displayName = "Body surface",
        swatchColor = Color(0xFFBA9B7D),
        description = "The body surface provides an outer anatomical reference. The integumentary system forms a protective barrier and contributes to sensation and temperature regulation.",
    ),
    CONNECTIVE(
        id = "connective",
        displayName = "Connective tissue",
        swatchColor = Color(0xFFAEC3BB),
        description = "Cartilage, ligaments, and other connective tissues support, connect, and separate structures. Their roles include stabilizing joints and distributing mechanical loads.",
    );

    companion object {
        private val byId = entries.associateBy { it.id }
        fun fromId(id: String): SystemId? = byId[id]

        val defaultVisible: Set<SystemId> = setOf(
            CARDIAC, SENSORY, SKELETAL, MUSCULAR, ARTERIAL, VENOUS, NERVOUS,
            RESPIRATORY, DIGESTIVE, URINARY, LYMPHATIC, ENDOCRINE, REPRODUCTIVE, CONNECTIVE,
        )
    }
}

enum class SystemRenderMode(val label: String) {
    SOLID("Solid"),
    XRAY("X-Ray"),
    HIDDEN("Hidden");

    fun next(): SystemRenderMode = when (this) {
        SOLID -> XRAY
        XRAY -> HIDDEN
        HIDDEN -> SOLID
    }
}

enum class CameraView(val jsValue: String, val shortLabel: String, val label: String) {
    THREE_QUARTER("three-quarter", "3/4", "Three-quarter"),
    FRONT("front", "F", "Front"),
    SIDE("side", "S", "Side"),
    BACK("back", "B", "Back"),
}

enum class AtlasPreset(val label: String) {
    DEFAULT("Default"),
    ALL("All"),
    SKELETON("Skeleton"),
    ORGANS("Organs"),
    NEUROVASCULAR("Vessels"),
}

data class AnatomicalPart(
    val id: String,
    val name: String,
    val conceptId: String,
    val system: SystemId,
)

data class AnatomicalConcept(
    val id: String,
    val name: String,
    val elements: List<String>,
)

data class AtlasCatalog(
    val parts: List<AnatomicalPart>,
    val partsById: Map<String, AnatomicalPart>,
    val concepts: List<AnatomicalConcept>,
    val conceptsById: Map<String, AnatomicalConcept>,
    val partCountsBySystem: Map<SystemId, Int>,
    val totalTriangles: Int,
)

data class SceneViewportInsets(
    val left: Int = 20,
    val top: Int = 96,
    val right: Int = 20,
    val bottom: Int = 200,
)

object AtlasExplanations {
    private val curated = mapOf(
        "heart" to "A muscular pump in the chest. Its right side sends blood to the lungs; its left side sends blood through the systemic circulation.",
        "liver" to "A large organ beneath the right side of the diaphragm. It processes absorbed nutrients, produces bile, and synthesizes many proteins carried in the blood.",
        "brain" to "The central organ of the nervous system. Its interconnected regions support perception, movement, memory, language, and the regulation of bodily functions.",
        "stomach" to "A muscular chamber between the esophagus and small intestine. It stores and mixes food with acid and enzymes before releasing it into the duodenum.",
        "spleen" to "A lymphoid organ in the upper left abdomen. It filters blood, removes aging blood cells, and participates in immune responses.",
        "pancreas" to "An abdominal organ with digestive and endocrine roles. It supplies enzymes to the small intestine and releases hormones including insulin and glucagon.",
        "urinary bladder" to "A muscular reservoir in the pelvis that stores urine arriving from the kidneys through the ureters.",
        "trachea" to "The main airway connecting the larynx to the bronchi. Its cartilage supports keep the airway open during breathing.",
        "diaphragm" to "A broad muscle separating the chest and abdomen. When it contracts, it increases chest volume and helps draw air into the lungs.",
    )

    fun resolve(name: String, system: SystemId): Pair<String, Boolean> {
        val exact = curated[name.lowercase()]
        return if (exact != null) {
            exact to true
        } else {
            system.description to false
        }
    }
}
