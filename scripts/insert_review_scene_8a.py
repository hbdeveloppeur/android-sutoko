#!/usr/bin/env python3
"""Inserts the waiter "leave a review" scene into chapter 8A of friendzone1,
in all 5 languages. Idempotent: skips languages already patched.

Graph change (identical in every language):
  before: 267 -> 269 , 268 -> 269
  after:  267 -> 400 , 268 -> 400
          400 -> 401 -> 402 -> 403 -> 404 -> {405, 406}
          405 -> 407([REVIEW]) -> 408 -> 269
          406 -> 409 -> 269
"""
import json
import os

BASE = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "..",
    "games", "friendzone1", "src", "main", "assets", "friendzone1_assets",
    "json", "chapters", "8A",
)

DIALOGUE = dict(time="", code="", wait="0")
INFO = dict(time=None, code=None, wait=0)

# id: (id_author, type, seen, sentence)
TEXTS = {
    "fr-FR": {
        400: (-1, 4, 2000, "Le serveur revient vers votre table"),
        401: (44, 1, 2000, "Puis-je vous demander quelque chose…"),
        402: (44, 1, 3000, "J'ai travaillé très, très dur pour tout ça…"),
        403: (44, 1, 3500, "Ça me rendrait tellement heureux si vous me laissiez un avis, un petit message que je pourrai relire plus tard…"),
        404: (42, 1, 3500, "Bien sûr, avec plaisir ! J'adore cette appli— euh… je veux dire, ce restaurant !"),
        405: (0, 1, 0, "Laisser un avis"),
        406: (0, 1, 0, "Ignorer"),
        407: (-1, 6, 0, "[REVIEW]"),
        408: (44, 1, 2500, "Merci infiniment ! Vous êtes adorables !"),
        409: (44, 1, 2500, "Ne vous inquiétez pas, pas de soucis ! Bonne soirée à vous deux !"),
    },
    "en-GB": {
        400: (-1, 4, 2000, "The waitress comes back to your table"),
        401: (44, 1, 2000, "May I ask you something…?"),
        402: (44, 1, 3000, "I worked so, so hard on all of this…"),
        403: (44, 1, 3500, "It would make me so happy if you left me a review, a little message I could read again later…"),
        404: (42, 1, 3500, "Of course we will! I love this app— uh… I mean, this restaurant!"),
        405: (0, 1, 0, "Leave a review"),
        406: (0, 1, 0, "Ignore"),
        407: (-1, 6, 0, "[REVIEW]"),
        408: (44, 1, 2500, "Thank you so, so much! You're the sweetest!"),
        409: (44, 1, 2500, "Don't worry, it's okay! Have a lovely evening, both of you!"),
    },
    "de-DE": {
        400: (-1, 4, 2000, "Der Kellner kommt zurück an euren Tisch"),
        401: (44, 1, 2000, "Darf ich euch etwas fragen…?"),
        402: (44, 1, 3000, "Ich habe so, so hart für all das hier gearbeitet…"),
        403: (44, 1, 3500, "Es würde mich so glücklich machen, wenn ihr mir eine Bewertung dalassen würdet, eine kleine Nachricht, die ich später nochmal lesen kann…"),
        404: (42, 1, 3500, "Klar machen wir das! Ich liebe diese App— äh… ich meine, dieses Restaurant!"),
        405: (0, 1, 0, "Bewertung dalassen"),
        406: (0, 1, 0, "Ignorieren"),
        407: (-1, 6, 0, "[REVIEW]"),
        408: (44, 1, 2500, "Vielen, vielen Dank! Ihr seid so lieb!"),
        409: (44, 1, 2500, "Kein Problem, alles gut! Euch beiden noch einen schönen Abend!"),
    },
    "es-ES": {
        400: (-1, 4, 2000, "El camarero vuelve a vuestra mesa"),
        401: (44, 1, 2000, "¿Puedo pediros algo…?"),
        402: (44, 1, 3000, "He trabajado muy, muy duro para todo esto…"),
        403: (44, 1, 3500, "Me haría tan feliz que me dejarais una reseña, un mensajito que pueda releer más tarde…"),
        404: (42, 1, 3500, "¡Claro que sí! Me encanta esta apli— eh… quiero decir, ¡este restaurante!"),
        405: (0, 1, 0, "Dejar una reseña"),
        406: (0, 1, 0, "Ignorar"),
        407: (-1, 6, 0, "[REVIEW]"),
        408: (44, 1, 2500, "¡Muchísimas gracias! ¡Sois encantadores!"),
        409: (44, 1, 2500, "¡No pasa nada, de verdad! ¡Que tengáis una buena noche los dos!"),
    },
    "es-419": {
        400: (-1, 4, 2000, "El mesero regresa a su mesa"),
        401: (44, 1, 2000, "¿Puedo pedirles algo…?"),
        402: (44, 1, 3000, "Trabajé muy, muy duro para todo esto…"),
        403: (44, 1, 3500, "Me haría tan feliz que me dejaran una reseña, un mensajito que pueda volver a leer más tarde…"),
        404: (42, 1, 3500, "¡Claro que sí! Me encanta esta app— eh… quiero decir, ¡este restaurante!"),
        405: (0, 1, 0, "Dejar una reseña"),
        406: (0, 1, 0, "Ignorar"),
        407: (-1, 6, 0, "[REVIEW]"),
        408: (44, 1, 2500, "¡Muchísimas gracias! ¡Son un amor!"),
        409: (44, 1, 2500, "No se preocupen, ¡está bien! ¡Que tengan una linda noche los dos!"),
    },
}

NEW_LINKS = [
    ("267", "400"), ("268", "400"),
    ("400", "401"), ("401", "402"), ("402", "403"), ("403", "404"),
    ("404", "405"), ("404", "406"),
    ("405", "407"), ("407", "408"), ("408", "269"),
    ("406", "409"), ("409", "269"),
]
REMOVED_LINKS = [("267", "269"), ("268", "269")]


def load(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def save(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))


def main():
    for lang, nodes in TEXTS.items():
        phrases_path = os.path.join(BASE, lang, "phrases-8A.json")
        links_path = os.path.join(BASE, lang, "links-8A.json")
        phrases = load(phrases_path)
        links = load(links_path)

        if any(p["id"] == 400 for p in phrases):
            print(f"{lang}: already patched, skipping")
            continue

        ids = {p["id"] for p in phrases}
        assert not (set(nodes) & ids), f"{lang}: id collision"

        for nid, (author, ntype, seen, sentence) in nodes.items():
            fmt = DIALOGUE if ntype == 1 else INFO
            phrases.append({
                "id": nid, "id_author": author, "time": fmt["time"],
                "code": fmt["code"], "sentence": sentence,
                "wait": fmt["wait"], "seen": seen, "type": ntype,
            })

        for src, dest in REMOVED_LINKS:
            link = {"src": src, "dest": dest, "c": 0}
            assert link in links, f"{lang}: missing link {src}->{dest}"
            links.remove(link)
        for src, dest in NEW_LINKS:
            links.append({"src": src, "dest": dest, "c": 0})

        save(phrases_path, phrases)
        save(links_path, links)
        print(f"{lang}: patched (+{len(nodes)} phrases, {len(NEW_LINKS)} links)")


if __name__ == "__main__":
    main()
