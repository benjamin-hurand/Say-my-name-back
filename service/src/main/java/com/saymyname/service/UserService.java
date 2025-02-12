package com.saymyname.service;

import com.saymyname.core.model.common.User;
import com.saymyname.persistence.dao.UserDao;
import com.saymyname.persistence.entity.CustomUserDetails;
import com.saymyname.persistence.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UserService implements UserDetailsService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private static final String[] FRENCH_ADJECTIVES = {
            "Intrépide", "Joyeux", "Brillant", "Astucieux", "Mystérieux", "Agile", "Vif",
            "Élégant", "Furtif", "Subtil", "Courageux", "Serein", "Rebel", "Rusé", "Majestueux",
            "Lumineux", "Malicieux", "Optimiste", "Héroïque", "Poétique", "Ébouriffant", "Sauvage",
            "Téméraire", "Envoûtant", "Pétillant", "Esprit", "Féerique", "Galant", "Imprévisible",
            "Gribouillé", "Échevelé", "Babouin", "Cacophonique", "Déjanté", "Élastique",
            "Frénétique", "Gigotant", "Hurluberlu", "Irrévérencieux", "Jovial", "Klaxon",
            "Lilliputien", "Mou", "Nasillard", "Oscillant", "Papillotant", "Quiproquo",
            "Rigolo", "Supersonique", "Trépidant", "Utopique", "Ventriloque", "Wagon",
            "Xylophone", "Yodel", "Zinzin"
    };

    private static final String[] FRENCH_NOUNS = {
            "Sorcier", "Ninja", "Rôdeur", "Pirate", "Robot", "Extraterrestre", "Monstre",
            "Chevalier", "Dragon", "Phénix", "Vampire", "Guerrier", "Fantôme", "Licorne", "Griffon",
            "Aventurier", "Baroudeur", "Corsaire", "Druide", "Émissaire", "Faune", "Gladiateur",
            "Héros", "Illusionniste", "Joueur", "Korrigan", "Loup", "Magicien", "Navigateur",
            "Oracle", "Paladin", "Quêteur", "Renard", "Souverain", "Triton", "Ursidé", "Voyageur",
            "Yéti", "Zéphyr",
            "Canard", "Hamster", "Potiron", "Cornichon", "Tambourin", "Escargot", "Chaussette",
            "Haricot", "Cactus", "Bretzel", "Chameau", "Dinosaure", "Flamant", "Girafe", "Hippocampe",
            "Iguane", "Jelly", "Kiwi", "Lama", "Muffin", "Nougat", "Omelette", "Pamplemousse",
            "Quokka", "Ratatouille", "Sandwich", "Tofu", "Ukulélé", "Vélociraptor", "Wombat",
            "Xérès", "Yak", "Zèbre"
    };
    private static final String[] ENGLISH_ADJECTIVES = {
            "Wacky", "Silly", "Bouncy", "Zany", "Goofy", "Whimsical", "Gigantic", "Tiny", "Fluffy",
            "Grumpy", "Smelly", "Loud", "Quiet", "Bright", "Dull", "Cheerful", "Gloomy", "Sparkly",
            "Dizzy", "Nimble", "Awkward", "Clumsy", "Brave", "Cowardly", "Energetic", "Lazy",
            "Frisky", "Sneaky", "Fuzzy", "Spiky", "Smooth", "Rough", "Jolly", "Cranky", "Happy-go-lucky",
            "Zippy", "Breezy", "Haphazard", "Mischievous", "Perky", "Sleepy", "Hyper", "Melancholy",
            "Chirpy", "Blabbering", "Flamboyant", "Nervous", "Jumpy", "Scruffy", "Polished", "Boisterous",
            "Squeaky", "Bubbly", "Twirly", "Hilarious", "Quirky", "Cuddly", "Eccentric", "Peppy",
            "Frivolous", "Luminous", "Snazzy", "Dapper", "Quaint", "Radiant", "Spunky", "Zestful",
            "Witty", "Hasty", "Chipper", "Giddy", "Loony", "Madcap", "Nifty", "Plucky", "Rambunctious",
            "Spirited", "Vivacious", "Wiggly", "Zippy", "Chubby", "Whiny", "Bashful", "Perplexing",
            "Bewildering", "Ebullient", "Jaunty", "Jubilant", "Kooky", "Raucous", "Rowdy", "Snappy",
            "Sprightly", "Spry", "Twinkling", "Unruly", "Winsome", "Zany"
    };
    private static final String[] ENGLISH_NOUNS = {
            "Penguin", "Banana", "Taco", "Unicorn", "Pickle", "Bubble", "Robot", "Zombie", "Alien",
            "Ninja", "Pirate", "Wizard", "Dragon", "Monster", "Fairy", "Elf", "Gnome", "Dinosaur",
            "Mermaid", "Phoenix", "Griffon", "Werewolf", "Yeti", "Bigfoot", "Goblin", "Troll",
            "Kraken", "Ogre", "Minotaur", "Sasquatch", "Chupacabra", "Leprechaun", "Manticore",
            "Pegasus", "Chimera", "Sphinx", "Cyclops", "Hydra", "Basilisk", "Behemoth", "Krampus",
            "Jackalope", "Cyborg", "Gryphon", "Hobbit", "Ent", "Gargoyle", "Lich", "Sprite", "Djinn",
            "Pixie", "Siren", "Wendigo", "Banshee", "Kitsune", "Gremlin", "Hippogriff", "Boggart",
            "Nessie", "Amphibian", "Butterfly", "Caterpillar", "Kangaroo", "Platypus", "Wombat",
            "Meerkat", "Koala", "Armadillo", "Porcupine", "Mongoose", "Weasel", "Raccoon", "Lynx",
            "Mole", "Chinchilla", "SugarGlider", "Capybara", "GuineaPig", "Ferret", "Panda",
            "Sloth", "Ocelot", "Aardvark", "Alligator", "Anteater", "Axolotl", "Baboon", "Bison",
            "Cheetah", "Chimpanzee", "Dodo", "Emu", "Falcon", "Gecko", "Giraffe", "Hippo", "Iguana",
            "Jellyfish", "Kookaburra", "Lemur", "Manatee", "Narwhal", "Octopus", "Pangolin", "Quokka",
            "Rhinoceros", "Squirrel", "Tapir", "Vulture", "Walrus", "Xerus", "Yak", "Zebra"
    };

    private Random random = new Random();

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateUniqueUsername(String language) {
        String username;
        int attempts = 0;
        do {
            username = generateRandomUsername(language);
            attempts++;
            if (attempts > 50) { // Limit the attempts to prevent infinite loops
                throw new IllegalStateException("Too many attempts to generate a unique username");
            }
        } while (userDao.checkIfUsernameExists(username));
        return username;
    }

    private String generateRandomUsername(String language) {
        String[] adjectives;
        String[] nouns;
        boolean adjectiveFirst;

        switch (language.toLowerCase()) {
            case "french":
                adjectives = FRENCH_ADJECTIVES;
                nouns = FRENCH_NOUNS;
                adjectiveFirst = random.nextBoolean();
                break;
            case "english":
            default:
                adjectives = ENGLISH_ADJECTIVES;
                nouns = ENGLISH_NOUNS;
                adjectiveFirst = true;
                break;
        }

        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];


        if (adjectiveFirst) {
            return adjective + '-' + noun;
        } else {
            return noun + '-' + adjective;
        }
    }


    public User save(User user) {
        // Encode le mot de passe avant de sauvegarder l'user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }

    public User setActive(User user) {
        // Encode le mot de passe avant de sauvegarder l'user
        user.setActive(true);
        return userDao.save(user);
    }

    public Boolean checkIfAccountExistsWithEmail(String email) {
        return userDao.checkIfEmailExists(email);
    }

    public Boolean checkIfAccountExistsWithUsername(String username) {
        return userDao.checkIfUsernameExists(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userDao.findEntityByEmailOrUsername(username);
        return new CustomUserDetails(user);
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    public User findByEmailOrUsername(String email) {
        return userDao.findByEmailOrUsername(email);
    }
}
