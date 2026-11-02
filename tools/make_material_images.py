from pathlib import Path
from PIL import Image, ImageDraw


OUT = Path(__file__).resolve().parents[1] / 'app/src/main/res/drawable-nodpi'
OUT.mkdir(exist_ok=True)


def canvas(bg):
    im = Image.new('RGB', (480, 360), bg)
    return im, ImageDraw.Draw(im)


def paper():
    im, d = canvas('#E9D9BB')
    d.rounded_rectangle((68, 154, 408, 298), 14, '#A87946')
    d.polygon([(68, 154), (126, 104), (408, 104), (408, 154)], fill='#D5AB74')
    d.rectangle((126, 104, 408, 245), '#D5AB74')
    d.line([(126, 104), (126, 245), (68, 298)], fill='#8D6337', width=6)
    d.line([(260, 104), (260, 245)], fill='#A87946', width=5)
    for x, y in [(125, 69), (153, 51), (181, 76)]:
        d.polygon([(x, y), (x + 134, y), (x + 117, y + 51), (x + 12, y + 51)], fill='#FFF9ED')
        for yy in (y + 17, y + 29):
            d.line((x + 22, yy, x + 102, yy), fill='#B7A994', width=3)
    return im


def plastic():
    im, d = canvas('#DCEEF0')
    for x, color in [(106, '#71B2C6'), (258, '#7BC8B6')]:
        d.rounded_rectangle((x + 34, 54, x + 86, 91), 5, '#407785')
        d.rounded_rectangle((x + 19, 86, x + 101, 110), 8, '#B5DDE6')
        d.polygon([(x + 19, 110), (x, 137), (x, 288), (x + 19, 313),
                   (x + 101, 313), (x + 120, 288), (x + 120, 137), (x + 101, 110)], fill=color)
        d.rounded_rectangle((x + 8, 174, x + 112, 230), 8, '#F8FCFA')
        d.line((x + 32, 145, x + 32, 278), fill='#C9EBEB', width=8)
    return im


def metal():
    im, d = canvas('#E5EAEC')
    for x, y, color in [(78, 96, '#A3B6BE'), (196, 66, '#C7D2D7'), (298, 116, '#8FA9B5')]:
        d.rounded_rectangle((x, y, x + 102, y + 199), 12, color)
        d.ellipse((x, y - 12, x + 102, y + 22), fill='#DCE6E9', outline='#82969F', width=4)
        d.ellipse((x + 24, y - 4, x + 78, y + 12), outline='#8A9FA7', width=3)
        d.rectangle((x + 12, y + 76, x + 90, y + 129), fill='#EAF5F3')
    return im


def glass():
    im, d = canvas('#E1F3E9')
    for x, color in [(90, '#7BC6A9'), (252, '#A5CC8A')]:
        d.rounded_rectangle((x + 39, 46, x + 93, 92), 6, '#4C8169')
        d.polygon([(x + 40, 92), (x + 18, 139), (x + 18, 286),
                   (x + 34, 316), (x + 98, 316), (x + 114, 286),
                   (x + 114, 139), (x + 92, 92)], fill=color)
        d.rounded_rectangle((x + 29, 192, x + 103, 244), 7, '#F4FBF2')
        d.line((x + 41, 110, x + 41, 179), fill='#C5EBD8', width=7)
    return im


def electronics():
    im, d = canvas('#E8E8F2')
    d.rounded_rectangle((67, 74, 322, 270), 15, '#4E6573')
    d.rectangle((84, 91, 305, 242), '#8FC0B5')
    d.polygon([(172, 270), (222, 270), (240, 307), (154, 307)], fill='#6F8590')
    d.rounded_rectangle((330, 125, 419, 291), 15, '#3E5967')
    d.rounded_rectangle((339, 140, 410, 261), 5, '#9ED4BF')
    d.ellipse((368, 269, 381, 282), fill='#B4C7CC')
    return im


def mixed():
    im, d = canvas('#ECF1E9')
    d.rounded_rectangle((65, 119, 248, 293), 12, '#BF9666')
    d.polygon([(65, 119), (101, 83), (248, 83), (248, 119)], fill='#DBB887')
    d.rounded_rectangle((280, 74, 339, 303), 18, '#78B9C2')
    d.rounded_rectangle((294, 49, 325, 79), 4, '#477F8A')
    d.rounded_rectangle((315, 181, 409, 301), 9, '#9EADB4')
    return im


for name, image in {
    'paper': paper(),
    'plastic': plastic(),
    'metal': metal(),
    'glass': glass(),
    'electronics': electronics(),
    'mixed': mixed(),
}.items():
    image.save(OUT / f'material_{name}.png', optimize=True)
