;;; test data

(ns keyboard-at-home.data)

;; needs srs work
(defn brown-detag [rdr]
  (-> (slurp rdr)
      (.replaceAll "/[^ ]+" "")))

(def hipster-ipsum
   "Tumblr VHS shoreditch echo park pitchfork DIY. Brunch master cleanse
   scenester mustache, locavore keytar tattooed wolf american apparel fixie
   whatever seitan shoreditch cliche viral. Wes anderson banh mi wayfarers
   keffiyeh salvia. Iphone vice lo-fi vinyl, twee fixie chambray ethical +1
   dreamcatcher carles thundercats terry richardson wes anderson. DIY
   sustainable quinoa dreamcatcher whatever, before they sold out banh mi.
   Vinyl cliche 8-bit keffiyeh. American apparel farm-to-table locavore,
   wayfarers marfa gentrify craft beer fanny pack readymade mixtape yr
   bicycle rights Austin.

   Wes anderson twee four loko you probably haven't heard of them vegan.
   Keytar squid +1, messenger bag lo-fi fixie ethical. Biodiesel carles
   iphone lo-fi readymade. Blog jean shorts dreamcatcher, gluten-free
   scenester trust fund vegan synth +1. Mlkshk lomo vice, carles ethical
   tattooed craft beer art party marfa. Farm-to-table aesthetic american
   apparel twee irony. Vinyl pitchfork williamsburg retro organic.

   Lomo keffiyeh master cleanse leggings, american apparel DIY quinoa. Craft
   beer fanny pack VHS, food truck four loko squid stumptown fap. Before they
   sold out single-origin coffee photo booth yr. Tumblr blog tattooed,
   skateboard jean shorts vice wayfarers stumptown. Butcher viral messenger
   bag vinyl beard, marfa chambray aesthetic single-origin coffee gentrify
   cardigan trust fund. Thundercats marfa photo booth sartorial. Gluten-free
   organic vinyl cosby sweater wayfarers, next level skateboard synth artisan
   DIY portland +1.

   Echo park wolf mixtape fap cliche, lomo brooklyn +1 marfa leggings.
   Biodiesel retro vinyl tumblr freegan you probably haven't heard of them.
   Before they sold out 3 wolf moon jean shorts gentrify art party put a bird
   on it. Next level craft beer mixtape 8-bit. You probably haven't heard of
   them letterpress jean shorts, mustache sartorial cliche tumblr raw denim
   banh mi master cleanse gentrify squid. American apparel whatever +1
   gluten-free mcsweeney's lomo, twee leggings you probably haven't heard of
   them sartorial raw denim iphone photo booth fanny pack. Gentrify biodiesel
   mlkshk messenger bag, bicycle rights brunch wolf sartorial.")


(def brown-scifi1 "Now that he knew himself to be self he was free to
    grok ever closer to his brothers , merge without let . integrity
    was and is and ever had been . stopped to cherish all his brother
    selves , the many threes-fulfilled on Mars , corporate and
    discorporate , the precious few on Earth -- the unknown powers of
    three on Earth that would be his to merge with and cherish now
    that at last long waiting he grokked and cherished himself .
    remained in trance ; ; was much to grok , loose ends to puzzle
    over and fit into his growing -- all that he had seen and heard
    and been at the Archangel Foster Tabernacle ( not just cusp when
    he and Digby had come face to face alone ) why Bishop Senator
    Boone made him warily uneasy , how Miss Dawn Ardent tasted like a
    water brother when she was not , the smell of goodness he had
    incompletely grokked in the jumping up and down and wailing --
    \n\n\tJubal's conversations coming and going -- Jubal's words
    troubled him most ; ; studied them , compared them with what he
    had been taught as a nestling , struggling to bridge between
    languages , the one he thought with and the one he was learning to
    think in . word `` church '' which turned up over and over again
    among Jubal's words gave him knotty difficulty ; ; was no Martian
    concept to match it -- unless one took `` church '' and `` worship
    '' and `` God '' and `` congregation '' and many other words and
    equated them to the totality of the only world he had known during
    growing-waiting then forced the concept back into English in that
    phrase which had been rejected ( by each differently ) by Jubal ,
    by Mahmoud , by Digby . Thou art God '' . was closer to
    understanding it in English now , although it could never have the
    inevitability of the Martian concept it stood for . his mind he
    spoke simultaneously the English sentence and the Martian word and
    felt closer grokking . it like a student telling himself that the
    jewel is in the lotus he sank into nirvana . midnight he speeded
    his heart , resumed normal breathing , ran down his check list ,
    uncurled and sat up . had been weary ; ; he felt light and gay and
    clear-headed , ready for the many actions he saw spreading out
    before him . felt a puppyish need for company as strong as his
    earlier necessity for quiet . stepped out into the hall , was
    delighted to encounter a water brother . Hi '' ! ! Oh . , Mike . ,
    you look chipper '' . I feel fine ! ! is everybody '' ? ? Asleep .
    and Stinky went home an hour ago and people started going to bed
    '' . Oh '' . felt disappointed that Mahmoud had left ; ; wanted to
    explain his new grokking . I ought to be asleep , too , but I felt
    like a snack . you hungry '' ? ? Sure , I'm hungry '' ! ! Come on
    , there's some cold chicken and we'll see what else '' . went
    downstairs , loaded a tray lavishly . Let's take it outside .
    plenty warm '' . A fine idea '' , Mike agreed . Warm enough to
    swim -- real Indian summer . switch on the floods '' . Don't
    bother '' , Mike answered . I'll carry the tray '' . could see in
    almost total darkness . said that his night-sight probably came
    from the conditions in which he had grown up , and Mike grokked
    this was true but grokked that there was more to it ; ; foster
    parents had taught him to see . for the night being warm , he
    would have been comfortable naked on Mount Everest but his water
    brothers had little tolerance for changes in temperature and
    pressure ; ; was considerate of their weakness , once he learned
    of it . he was looking forward to snow -- seeing for himself that
    each tiny crystal of the water of life was a unique individual ,
    as he had read -- walking barefoot , rolling in it . the meantime
    he was pleased with the warm night and the still more pleasing
    company of his water brother . Okay , take the tray . switch on
    the underwater lights . be plenty to eat by '' . Fine '' . liked
    having light up through the ripples ; ; was a goodness , beauty .
    picnicked by the pool , then lay back on the grass and looked at
    stars . Mike , there's Mars . is Mars , isn't it ? ? Antares '' ?
    ? It is Mars '' . Mike ? ? are they doing on Mars '' ? ? hesitated
    ; ; question was too wide for the sparse English language . On the
    side toward the horizon -- the southern hemisphere -- it is spring
    ; ; are being taught to grow '' . ' Taught to grow ' '' ? ?
    hesitated . Larry teaches plants to grow . have helped him . my
    people -- Martians , I mean ; ; now grok you are my people --
    teach plants another way . the other hemisphere it is growing
    colder and nymphs , those who stayed alive through the summer ,
    are being brought into nests for quickening and more growing '' .
    thought . Of the humans we left at the equator , one has
    discorporated and the others are sad '' . , I heard it in the news
    '' . had not heard it ; ; had not known it until asked . They
    should not be sad . Booker T. W. Jones Food Technician First Class
    is not sad ; ; Old Ones have cherished him '' . You knew him '' ?
    ? Yes . had his own face , dark and beautiful . he was homesick ''
    . Oh , dear ! ! do you ever get homesick ? ? Mars '' ? ? At first
    I was homesick '' , he answered . I was lonely always '' . rolled
    toward her and took her in his arms . But now I am not lonely .
    grok I shall never be lonely again '' . Mike darling '' -- They
    kissed , and went on kissing . his water brother said breathlessly
    . Oh , my ! ! was almost worse than the first time '' . You are
    all right , my brother '' ? ? Yes . indeed . me again '' . long
    time later , by cosmic clock , she said , `` Mike ? ? that -- I
    mean , ' Do you know ' '' -- \n\n\t`` I know . is for growing
    closer . we grow closer '' . Well I've been ready a long time --
    goodness , we all have , but never mind , dear ; ; just a little .
    help '' . they merged , grokking together , Mike said softly and
    triumphantly : `` Thou art God '' . answer was not in words . , as
    their grokking made them ever closer and Mike felt himself almost
    ready to discorporate her voice called him back : `` Oh ! ! ! !
    art God '' ! ! We grok God '' . . Mars humans were building
    pressure domes for the male and female party that would arrive by
    next ship . went faster than scheduled as the Martians were
    helpful . of the time saved was spent on a preliminary estimate
    for a long-distance plan to free bound oxygen in the sands of Mars
    to make the planet more friendly to future human generations . Old
    Ones neither helped nor hindered this plan ; ; was not yet .
    meditations were approaching a violent cusp that would shape
    Martian art for many millennia . Earth elections continued and a
    very advanced poet published a limited edition of verse consisting
    entirely of punctuation marks and spaces ; ; magazine reviewed it
    and suggested that the Federation Assembly Daily Record should be
    translated into the medium . colossal campaign opened to sell more
    sexual organs of plants and Mrs. Joseph ( `` Shadow of Greatness
    '' ) Douglas was quoted as saying : `` I would no more sit down
    without flowers on my table than without serviettes '' . Tibetan
    swami from Palermo , Sicily , announced in Beverly Hills a newly
    discovered , ancient yoga discipline for ripple breathing which
    increased both pranha and cosmic attraction between sexes . chelas
    were required to assume the matsyendra posture dressed in
    hand-woven diapers while he read aloud from Rig-Veda and an
    assistant guru examined their purses in another room -- nothing
    was stolen ; ; purpose was less immediate . President of the
    United States proclaimed the first Sunday in November as ``
    National Grandmothers' Day '' and urged America to say it with
    flowers . funeral parlor chain was indicted for price-cutting .
    bishops , after secret conclave , announced the Church's second
    Major Miracle : Supreme Bishop Digby had been translated bodily to
    Heaven and spot-promoted to Archangel , ranking with-but-after
    Archangel Foster . glorious news had been held up pending Heavenly
    confirmation of the elevation of a new Supreme Bishop , Huey Short
    -- a candidate accepted by the Boone faction after lots had been
    cast repeatedly . and Hoy published identical denunciations of
    Short's elevation , l'Osservatore Romano and the Christian Science
    Monitor ignored it , Times of India snickered at it , and the
    Manchester Guardian simply reported it -- the Fosterites in
    England were few but extremely militant . was not pleased with his
    promotion . Man from Mars had interrupted him with his work half
    finished -- and that stupid jackass Short was certain to louse it
    up . listened with angelic patience until Digby ran down , then
    said , `` Listen , junior , you're an angel now -- so forget it .
    is no time for recriminations . too were a stupid jackass until
    you poisoned me . you did well enough . that Short is Supreme
    Bishop he'll do all right , he can't help it . as with the Popes .
    of them were warts until they got promoted . with one of them , go
    ahead -- there's no professional jealousy here '' . calmed down ,
    but made one request . shook his halo . You can't touch him .
    shouldn't have tried to . , you can submit a requisition for a
    miracle if you want to make a fool of yourself . , I'm telling you
    , it'll be turned down -- you don't understand the System yet .
    Martians have their own setup , different from ours , and as long
    as they need him , we can't touch him . run their show their way
    -- the Universe has variety , something for everybody -- a fact
    you field workers often miss '' . You mean this punk can brush me
    aside and I've got to hold still for it '' ? ? I held still for
    the same thing , didn't I ? ? helping you now , am I not ? ? look
    , there's work to be done and lots of it . Boss wants performance
    , not gripes . you need a Day off to calm down , duck over to the
    Muslim Paradise and take it . , straighten your halo , square your
    wings , and dig in . sooner you act like an angel the quicker
    you'll feel angelic . Happy , junior '' ! ! heaved a deep ethereal
    sigh . Okay , I'm Happy . do I start '' ? ? did not hear of
    Digby's disappearance when it was announced , and , when he did ,
    while he had a fleeting suspicion , he dismissed it ; ; Mike had
    had a finger in it , he had gotten away with it -- and what
    happened to supreme bishops worried Jubal not at all as long as he
    wasn't bothered . household had gone through an upset . deduced
    what had happened but did not know with whom -- and didn't want to
    inquire . was of legal age and presumed able to defend himself in
    the clinches . , it was high time the boy was salted . couldn't
    reconstruct the crime from the way the girls behaved because
    patterns kept shifting -- ABC vs D , then BCD vs A or AB vs CD ,
    or AD vs CB , through all ways that four women can gang up on each
    other . continued most of the week following that ill-starred trip
    to church , during which period Mike stayed in his room and
    usually in a trance so deep that Jubal would have pronounced him
    dead had he not seen it before . would not have minded it if
    service had not gone to pieces . girls seemed to spend half their
    time tiptoeing in `` to see if Mike was all right '' and they were
    too preoccupied to cook , much less be secretaries . rock-steady
    Anne -- Hell , Anne was the worst ! ! , subject to unexplained
    tears Jubal would have bet his life that if Anne were to witness
    the Second Coming , she would memorize date , time , personae ,
    events , and barometric pressure without batting her calm blue
    eyes .")

(def brown-humor1 "Pueri aquam de silvas ad agricolas portant , a
    delightful vignette set in the unforgettable epoch of pre-Punic
    War Rome . , the hero , is beset from all sides by the problems of
    approaching manhood . story opens on the eve of his fifty-third
    birthday , as he prepares for the two weeks of festivities that
    are to follow . , a messenger arrives and , just before collapsing
    dead at his feet , informs him that the Saracens have invaded
    Silesia , the home province of his affianced . at once cancels the
    celebrations and , buckling on his scimitar , stumbles blindly
    from the house , where he is hit and killed by a passing oxcart .
    Albany Civic Opera's presentation of Spumoni's immortal Il Sevigli
    del Spegititgninino , with guest contralto Hattie Sforzt . unusual
    , if not extraordinary , rendering of the classic myth that
    involves the rescue of Prometheus from the rock by the U.S.
    Cavalry was given last week in the warehouse of the Albany Leather
    Conduit Company amid cheers of `` Hubba hubba '' and `` Yalagaloo
    pip pip '' ! ! a `` busy '' overture , the curtain rises on a farm
    scene -- the Ranavan Valley in northern Maine . dead armadillo ,
    the sole occupant of the stage , symbolizes the crisis and
    destruction of the Old Order . Order , acted and atonally sung by
    Grunnfeu Arapacis , the lovely Serbantian import , then entered
    and delivered the well-known invocation to the god Phineoppus ,
    whereupon the stage is quite unexpectedly visited by a company of
    wandering Gorshek priests , symbolizing Love , Lust , Prudence and
    General Motors , respectively . to the myth , Old Order then
    vanishes at stage left and reappears at extreme stage right , but
    Director Shuz skillfully sidesteps the rather gooshey problem of
    stage effects by simply having Miss Arapacis walk across the stage
    . night he saw it , a rather unpleasant situation arose when the
    soloist refused to approach the armadillo , complaining -- in
    ad-lib -- that `` it smelled '' . caught the early train to New
    York . Dharma Dictionary , a list of highly unusual terms used in
    connection with Eurasian proto-senility cults . somewhat off the
    beaten track , to be sure , but therein lies its variety and charm
    . example , probably very few people know that the word ``
    visrhanik '' that is bantered about so much today stems from the
    verb `` bouanahsha '' : to salivate . , and equally fascinating ,
    is the news that such unlikely synonyms as `` pratakku '' , ``
    sweathruna '' , and the tongue-twister ``
    nnuolapertar-it-vuh-karti-birifw- '' all originated in the same
    village in Bathar-on-Walli Province and are all used to express
    sentiments concerning British `` imperialism '' . terms are fairly
    safe to use on this side of the ocean , but before you start
    spouting them to your date , it might be best to find out if he
    was a member of Major Pockmanster's Delhi Regiment , since
    resentment toward the natives was reportedly very high in that
    outfit . breeze and chancellor Neitzbohr , a movie melodrama that
    concerns the attempts of a West German politician to woo a plaster
    cast of the Apollo Belvedere . you have doubtless guessed already
    , the plot is plastered with Freudian , Jungian , and Meinckian
    theory . example , when the film is only four minutes old ,
    Neitzbohr refers to a small , Victorian piano stool as ``
    Wilhelmina '' , and we are thereupon subjected to a flashback that
    informs us that this very piano stool was once used by an
    epileptic governess whose name , of course , was Doris ( the
    English equivalent , when passed through middle-Gaelic derivations
    , of Wilhelmina ) . the remainder of the movie , Chancellor
    Neitzbohr proceeds to lash the piano stool with a slat from a
    Venetian blind that used to hang in the pre-war Reichstag . this
    manner , he seeks to expunge from his own soul the guilt pangs
    caused by his personal assaults against the English at Dunkirk .
    we find out at the end , it is not the stool ( symbolizing Doris ,
    therefore the English ) that he is punishing but the piece of
    Venetian blind . , when the slat finally shatters , we see him
    count the fragments , all the while muttering , `` He loves me ,
    he loves me not '' . a few tortuous moments of wondering who `` he
    '' is , the camera pans across the room to the plaster statue ,
    and we realize that Neitzbohr is trying to redeem himself in the
    eyes of a mute piece of sculpture . effect , needless to say , is
    almost terrifying , and though at times a bit obscure , the film
    is certainly a much-needed catharsis for the `` repressed ''
    movie-goer . music of Bini SalFininistas , capital LP Ab63711-r ,
    one of the rare recordings of this titanic , yet unsung , composer
    . persons who were lucky enough to see and hear the performance of
    his work at the Brest-Silevniov Festival in August , 1916 , will
    certainly welcome his return to public notice ; ; it is not
    unlikely that , even as the great Bach lay dormant for so many
    years , so has the erudite , ingenious SalFininistas passed
    through his `` purgatory '' of neglect . now , under the guidance
    of the contemporary composer Marc Schlek , Jr. , a major revival
    is under way . he leads the Neurenschatz Skolkau Orchestra ,
    Schlek gives a tremendously inspired performance of both the
    Baslot and Rattzhenfuut concertos , including the controversial
    Tschilwyk cadenza , which was included at the conductor's
    insistence . major portion of the credit should also go to
    flautist Haumd for his rendering of the almost impossible ``
    Indianapolis '' movement in the Baslot . only was Haumd's
    intonation and phrasing without flaw , but he seemed to take every
    tonal eccentricity in stride . example , to move ( as the score
    requires ) from the lowest F-major register up to a barely audible
    N minor in four seconds , not skipping , at the same time , even
    one of the 407 fingerings , seems a feat too absurd to consider ,
    and it is to the flautist's credit that he remained silent
    throughout the passage . would have preferred , however , to have
    had the rest of the orchestra refrain from laughing at this and
    other spots on the recording , since it mars an otherwise sober ,
    if not lofty , performance . Broadway itself becomes increasingly
    weighted down by trite , heavy-handed , commercially successful
    musicals and inspirational problem dramas , the American theatre
    is going through an inexorable renaissance in that nebulous area
    known as `` off-Broadway '' . the last two years , this frontier
    of the arts has produced a number of so-called `` non-dramas ''
    which have left indelible , bittersweet impressions on the psyche
    of this veteran theatregoer . latest and , significantly ,
    greatest fruit of this theatrical vine is The , an adaptation of
    Basho's classic frog-haiku by Roger Entwhistle , a former
    University of Maryland chemistry instructor . the play does show a
    certain structural amateurishness ( there are eleven acts varying
    in length from twenty-five seconds to an hour and a half ) , the
    statement it makes concerning the ceaseless yearning and searching
    of youth is profound and worthy of our attention . action centers
    about a group of outspoken and offbeat students sitting around a
    table in a cafeteria and their collective and ultimately fruitless
    search for a cup of hot coffee . are relentlessly rebuffed on all
    sides by a waitress , the police , and an intruding government
    tutor . innocence that they tried to conceal at the beginning is
    clearly destroyed forever when one of them , asking for a piece of
    lemon-meringue pie , gets a plate of English muffins instead . the
    theatre after the performance , I had a flash of intuition that
    life , after all ( as Rilke said ) , is just a search for the
    nonexistent cup of hot coffee , and that this unpretentious ,
    moving , clever , bitter slice of life was the greatest thing to
    happen to the American theatre since Brooks Atkinson retired . but
    still precocious , French feline enfant terrible Francoisette
    Lagoon has succeeded in shocking jaded old Paris again , this time
    with a sexy ballet scenario called The Lascivious Interlude , the
    story of a nymphomaniac trip-hammer operator who falls hopelessly
    in love with a middle-aged steam shovel . biting , pithy parable
    of the all-pervading hollowness of modern life , the piece has
    been set by Mlle Lagoon to a sumptuous score ( a single motif
    played over and over by four thousand French horns ) by
    existentialist hot-shot Jean-Paul Sartre . , lovely Yvette Chadroe
    plays the nymphomaniac engagingly . since Bambi , and , more
    recently , Born Free , there have been a lot of books about
    animals , but few compare with Max Fink's wry , understated ,
    charming , and immensely readable My Friend , the Quizzical
    Salamander . in the modern style of a `` confession '' , Fink
    tells in exquisite detail how he came to know , and , more
    important , love his mother's pet salamander , Alicia . is not an
    entirely happy book , as Mrs. Fink soon becomes jealous of Alicia
    and , in retaliation , refuses to continue to scrape the algae off
    her glass . , in a fit of despair , takes Alicia and runs off for
    two marvelous weeks in Burbank ( Fink calls it `` the most
    wonderful and lovely fourteen days in my whole life '' ) , at the
    end of which Alicia tragically contracts Parkinson's disease and
    dies . brief resume hardly does the book justice , but I heartily
    recommend it to all those who are engages with the major problems
    of our time . in the Grand Tradition , along with mah-jongg ,
    seems to be staging a well-deserved comeback . this country , the
    two guiding lights are , without doubt , Felix Fing and Anna
    Pulova . , a lean , chiseled , impeccable gentleman of the old
    school who was once mistaken on the street for Sir Cedric
    Hardwicke , is responsible for the rediscovery of Verdi's earliest
    , most raucous opera , Nabisco , a sumptuous bout-de-souffle with
    a haunting leitmotiv that struck me as being highly reminiscent of
    the Mudugno version of `` Volare '' . Pulova has a voice that
    Maria Callas once described as `` like chipping teeth with a screw
    driver '' , and her round , opalescent face becomes fascinatingly
    reflective of the emotions demanded by the role of Rosalie .
    Champs Elysees is literally littered this summer with the
    prostrate bodies of France's beat-up beatnik jeunes filles . of
    all this commotion : squat , pug-nosed , balding , hopelessly ugly
    Jean-Pierre Bravado , a Bogartian figure , who plays a sadistic ,
    amoral , philosophic Tasti-Freeze salesman in old New-Waver
    Fredrico de Mille Rossilini's endlessly provocative film , A Sour
    Sponge . has been alternately described as `` a symbol of the new
    grandeur of France and myself '' ( De Gaulle ) and `` a decadent ,
    disgusting slob '' ! ! Norman Mailer ) , but no one can deny that
    the screen crackles with electricity whenever he is on it . to
    stardom along with him , Margo Felicity Brighetti , a luscious and
    curvaceously beguiling Italian starlet , turns in a creditable
    performance as an airplane mechanic . battle of the drib-drool
    continues , but most of New York's knowing sophisticates of
    Abstract Expressionism are stamping their feet impatiently in
    expectation of V ( for Vindication ) Day , September first , when
    Augustus Quasimodo's first one-man show opens at the Guggenheim .
    have heard that after seeing Mr. Quasimodo's work it will be
    virtually impossible to deny the artistic validity and importance
    of the whole abstract movement . it is thought by many who think
    about such things that Quasimodo is the logical culmination of a
    school that started with Monet , progressed through Kandinsky and
    the cubist Picasso , and blossomed just recently in Pollock and De
    Kooning . defines his own art as `` the search for what is not
    there '' . I paint the nothing '' , he said once to Franz Kline
    and myself , `` the nothing that is behind the something , the
    inexpressible , unpaintable ' tick ' in the unconscious , the '
    spirit ' of the moment resting forever , suspended like a huge
    balloon , in non-time '' . is his relentlessness and unwaivering
    adherence to this revolutionary artistic philosophy that has
    enabled him to paint such pictures as `` The Invasion of Cuba '' .
    this work , his use of non-color is startling and skillful . sweep
    of space , the delicate counterbalance of the white masses , the
    over-all completeness and unity , the originality and imagination
    , all entitle it to be called an authentic masterpiece . asked
    Quasimodo recently how he accomplished this , and he replied that
    he had painted his model `` a beautiful shade of red and then had
    her breathe on the canvas '' , which was his typical
    tongue-in-cheek way of chiding me for my lack of sensitivity .")

(def brown-humor2 "I realized that Hamlet was faced with an entirely
     different problem , but his agony could have been no greater .
     most that was accomplished was adding Mrs. Beige's tray to the
     dish pile , and by means of repeated threats , on an ascending
     scale , seeing that the girls dressed themselves , after a
     fashion . was saved from making the decision as the phone rang ,
     and the girls were upon me instantly . a household hint : if you
     can't find your children , and get tired of calling them , pick
     up the phone . matter if your children are at the movies , in
     school , visiting their grandmother , or on a field trip in some
     distant city , they will be upon you magically within seconds
     after you pick up the phone . and Miranda twined themselves
     around me , murmuring endearments . climbed onto a stool and
     clutched the hand with which I was trying to hold the phone ,
     claiming my immediate attention on grounds of extreme emergency .
     managing to get out a cool , poised , `` Won't you hold on a
     second , please '' , I covered up the mouthpiece , and with more
     warmth and less poise , gave a quick lecture on crime and
     punishment , mostly the latter , including Devil's Island and the
     remoter reaches of Siberia . promised to illustrate the lecture ,
     if they so much as breathed till after the call was completed .
     into the phone again and recognizing the caller , I resumed my
     everyday voice . we were deep in a conversation that was
     interrupted many times by little things like Jennie's holding her
     breath and pretending to black out , Miranda's dumping the
     contents of the sugar bowl on the table , and various screeches ,
     thuds , and giggles . the circumstances , I had difficulty
     keeping up with the conversation on the phone , but when I hung
     up I was reasonably certain that Francesca had wanted to remind
     me of our town meeting the next evening , and how important it
     was that Hank and I be there . discovered that the girls had
     shrewdly vacated the kitchen , and were playing quietly in the
     living room . seemed that I would be the gainer if I accepted the
     peace and quiet , instead of carrying out my threats . to get
     something done , I started in on the dishes . . not saying it
     right . I meant to say was that I started to start in on the
     dishes by gathering them all together in the kitchen sink .
     looked so formidable , however , so demanding , that I found
     myself staring at them in dismay and starting to woolgather again
     , this time about Francesca and her husband . about them , I
     thought . and Herbert were among the few people we knew in
     Catatonia . didn't even know them till about a month after we
     moved -- at that time , they had called on us , after I met Fran
     at a PTA meeting , and had taken us in hand socially . had been
     kind to us and we were indebted to them for one or two pleasant
     dinners , and for information as to where to shop , which dentist
     , doctor , plumber , and sitter to call ( not that there was much
     of a choice , since Catatonia was just a village ; ; yellow pages
     of the telephone book were amazingly thin ) . were ``
     personalities '' . , an expert on narrow ties , thin lapels , and
     swatches , was men's fashion editor of Parvenu , the weekly
     magazine with the tremendous circulation . and he had met about
     two years after she had arrived in Manhattan from Nebraska , or
     was it Wyoming ? ? was the daughter and sole heiress of either a
     cattle baron or an oil millionaire and , having arrived in New
     York with a big bank roll , became a dabbler in various fields .
     patronized Greenwich Village artists for awhile , then put some
     money into a Broadway show which was successful ( terrible , but
     successful ) . was during her `` writing '' period that she and
     Herb met and decided that they were in love . were married at a
     lavish ceremony which was duly recorded in Parvenu and all other
     magazines and newspapers , and then they honeymooned in Bermuda .
     , not Bermuda . was not in style that year . had honeymooned in
     Rome ; ; was very high on Rome that year . had bought their house
     in Catatonia after investigating all the regions of suburbia
     surrounding New York ; ; had chosen Catatonia because of its
     reputation for excellent schools , beaches , and abundance of
     names . You are bound to get involved with people when you have
     children '' , Fran had told me at our first meeting , `` so it is
     good to know that those with whom you get involved are not just
     dreary little housewives and dull husbands , but People Who Do
     Things '' . admired their easy way of doing things but I couldn't
     escape an uneasiness at their way of always doing the right
     things . house was a centuries-old Colonial which they had had
     restored ( guided by an eminent architect ) and updated , and
     added on to . had a gourmet's corner ( instead of a kitchen ) , a
     breakfast room , a luncheon room , a dining room , a sitting room
     , a room for standing up , a party room , dressing rooms for
     everybody , even a room for mud . was all set up so there would
     be no dust anywhere and so that their children would color in the
     coloring room , paint in the painting room , play with blocks in
     the block house , and do all the other things in the proper rooms
     at exactly the right time . two boys were `` well adjusted '' and
     , like their parents , always did the right thing at the right
     time and damn the consequences . and Herbert considered
     themselves violently nonconformist and showed the world they were
     by filling their Colonial house with contemporary furniture and
     paintings and other art objects ( expensive , but not necessarily
     valuable , contemporary things ) . flaunted her independence by
     rebelling against the Catatonia uniform of Bermuda shorts and
     knee-length socks by wearing Bermuda shorts and knee-length socks
     in colors ; ; pinks and plaids and vivid stripes . she even wore
     the uniform in solid , unrelieved black , and with her blonde
     hair cut so closely , wearing this uniform , she strongly
     resembled a member of the SS. . one could dislike them , I
     thought . , though , they did not seem quite human . seemed ,
     indeed , that their house was not so much a home , but rather a
     perfect stage set , and that they were actors who had been handed
     fat roles in a successful play , and had talent enough to fill
     the roles competently , with nice understatement . the only
     enthusiasm they showed was when they were discussing `` names ''
     ; ; brand names . should hear the reverence in Fran's voice when
     she said `` Baccarat '' or `` Steuben '' or `` Madame Alexander
     '' . always let it be known that there was wine in the pot roast
     or that the chicken had been marinated in brandy , and that
     Koussevitzky's second cousin was an intimate of theirs . wouldn't
     have wasted time puzzling over this couple were it not for my
     fear that all the other inhabitants of Catatonia were equally
     unreal . couldn't feel at home among them . Francesca , there was
     Blanche . was pleasant and charming , but Blanche was sweet . ,
     Blanche was very , very sweet -- being in her company was like
     being drowned in warm , melted marshmallows . had once been a
     witness when Blanche had smiled and said with only minimum
     ruefulness , `` Oh , my souffle has collapsed '' . knows how a
     real , red-blooded woman would react to such a catastrophe ! !
     Blanche had been honest , she would have yelled , slammed at
     least a couple of doors , and thrown a few little , valueless
     things . dear me , no ; ; Blanche . five minutes with Blanche ,
     one might welcome the astringency of Grazie , who was a sort of
     Gwen Cafritz to Francesca's Perle Mesta . and Grazie were
     habitual committee chairmen and they usually managed to be
     elected co-chairmen , equal bosses , of whatever PTA or civic
     project was being launched . were inseparable , not because they
     were fond of each other , but because they wanted to keep an eye
     on each other , as they were keen rivals for social leadership .
     was mean : quietly mean , and bitterly , unfunnily sarcastic . it
     was who had looked to see if I was wearing shoes upon learning
     that I couldn't drive . had a small , slick head and her hair and
     skin were the color of golden toast . lived in an ultra-modern
     house whose decoration , appointments , paint , and even pets
     were chosen to complement her coloring ; ; pets were a couple of
     Siamese cats . uniform was of rich , raw silk , in a shade which
     matched her hair , skin , housepaint , and cats , and since she
     was so thin as to be almost shapeless , she rather resembled a
     frozen fish stick . husbands of these women and others I had met
     in Catatonia were distinguished only in that they were , to me at
     least , indistinguishable . couldn't tell one from the other .
     Herbert , they were all in communications : radio , television ,
     magazines , and advertising . or two were writers of books ; ;
     were fellows of finite charm . had developed a hair-trigger
     chuckle and the habit of saying `` zounds '' ! ! deference to
     country-squirehood . never thought I'd live to hear people
     chuckle and say `` zounds '' ! ! real life . wouldn't have missed
     it for anything . were `` sincere '' -- men of the too-hearty
     handclasp and the urgent smile . boys acknowledged an
     introduction to anybody by gently pressing one of his hands in
     both of theirs , while they gazed , misty-eyed with care , into
     the eyes of the person they were meeting . such unadulterated
     love , for a total stranger , be credited ? ? were always leaping
     to light cigarettes , open car doors , fill plates or glasses ,
     and I mistrusted the whole lot of them to the same degree that I
     mistrusted bake shops that called themselves `` Sanitary Bake
     Shops '' . O ! ! ! ! I thought , and wondered what kind of
     homesteads such odd pioneers would establish in this suburban
     frontier ; ; who looked like off-duty gardeners even at
     parent-teacher conferences and who never called the school
     principal `` Mister '' . sighed , thinking that among other
     things , people here seemed to be those who would have to cut
     down if they earned less than $85,000 yearly ; ; who would give
     their teeth for a chance to get on `` Person to Person '' ; ; who
     thought it was nice to be important , but not important to be
     nice ; ; were more ingratiating than gracious , more
     personalities than persons . my estimation , they were people who
     read Daphne du Maurier , and discussed Kafka ; ; , not discussed
     him exactly , but said , `` Kafka '' ! ! and raised their eyes ,
     as if they were at a loss to describe how they felt about Kafka ,
     which they were , because they had no opinions about Kafka , not
     having read Kafka . were , I felt , people invariably trying to
     prove not who , but what they were , and trying to determine what
     , not who , others were . aware that it was nearly lunchtime , I
     brought myself back to the tasks at hand . made plans for the
     afternoon -- doing the breakfast and luncheon dishes all at once
     , making the beds , and then maybe painting the kitchen . , I
     remembered that the girls had had a banana for dessert every day
     for the last week . Bananas '' ! ! had shouted each time .
     They're not dessert ! ! not even food . just something you're
     supposed to put on cereal for breakfast '' . dug around and found
     a mix , and was able to surprise them with a devil's-food cake
     with chocolate icing . Sometimes I think you need only one rule
     for cooking : if you can't put garlic in it , put chocolate in it
     . \n\n\tThe cake was received in a stunned silence that was
     evidence in itself of the dearth of taste thrills Mama had been
     providing . Jennie closed her eyes , stretched forth her arms ,
     and said : `` Take my hand , Louise ; ; a stranger in paradise
     ''.")

(def ^:export fitness (str brown-humor2 brown-humor1 brown-scifi1))

