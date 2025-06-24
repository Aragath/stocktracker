// import packages
const express = require('express');
const cors = require('cors');
const path = require(`path`);
const mongoose = require('mongoose');
const { Double, ObjectId } = require('mongodb');

const FINNHUB_KEY = "";
const POLY_KEY = "";

const app = express();

app.use(cors()); // Enable CORS middleware
app.use(express.urlencoded({extended: true})); // Set app to use Express urlencoded middleware
app.use(express.json()); // Set app to use Express json middleware

// Listen to the App Engine-specified port, or 8080 otherwise
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`Server listening on port ${PORT}...`);
});

// get autocomplete
app.get('/auto/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try {
    const response = await fetch(`https://finnhub.io/api/v1/search?q=${ticker}&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub Autocomplete API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub Autocomplete API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get profile data
app.get('/profile/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try {
    const response = await fetch(`https://finnhub.io/api/v1/stock/profile2?symbol=${ticker}&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub profile API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub profile API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get hourly data
app.get('/hourly/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  
  // today
  const to = new Date().toISOString().split('T')[0];
  const today = new Date();
  // 5 days ago
  today.setDate(today.getDate() - 5);

  const from = today.toISOString().split('T')[0];
  try {
    const response = await fetch(`https://api.polygon.io/v2/aggs/ticker/${ticker}/range/1/hour/${from}/${to}?adjusted=true&sort=asc&apiKey=${POLY_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Polygon Historical API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Polygon Historical API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get historical data
app.get('/history/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  // 2 years ago
  const to = new Date().toISOString().split('T')[0];
  const twoYearsAgo = new Date();
  twoYearsAgo.setFullYear(twoYearsAgo.getFullYear() - 2);

  const from = twoYearsAgo.toISOString().split('T')[0];
  try {
    const response = await fetch(`https://api.polygon.io/v2/aggs/ticker/${ticker}/range/1/day/${from}/${to}?adjusted=true&sort=asc&apiKey=${POLY_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Polygon Historical API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Polygon Historical API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get stock quote
app.get('/quote/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try {
    const response = await fetch(`https://finnhub.io/api/v1/quote?symbol=${ticker}&token=${FINNHUB_KEY}`); 
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub Quote API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub Quote API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get news
app.get('/news/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  // 7 days ago
  const to = new Date().toISOString().split('T')[0];
  const today = new Date();
  today.setDate(today.getDate() - 7);

  const from = today.toISOString().split('T')[0];
  try {
    const response = await fetch(`https://finnhub.io/api/v1/company-news?symbol=${ticker}&from=${from}&to=${to}&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub News API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub News API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get trends
app.get('/trends/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try {
    const response = await fetch(`https://finnhub.io/api/v1/stock/recommendation?symbol=${ticker}&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub Trends API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub Trends API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get insider sentiment
app.get('/insider/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try { 
    const response = await fetch(`https://finnhub.io/api/v1/stock/insider-sentiment?symbol=${ticker}&from=2022-01-01&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub Insider API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub Insider API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get company peers
app.get('/peers/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try {
    const response = await fetch(`https://finnhub.io/api/v1/stock/peers?symbol=${ticker}&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub Peers API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub Peers API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// get company earnings
app.get('/earnings/:ticker', async (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  try {
    const response = await fetch(`https://finnhub.io/api/v1/stock/earnings?symbol=${ticker}&token=${FINNHUB_KEY}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch data from Finnhub Earnings API: ${response.statusText}`);
    } 
    const data = await response.json();
    res.json(data);
  } catch (error) {
    console.error('Error fetching data from Finnhub Earnings API:', error.message);
    res.status(500).send('Internal Server Error');
  }
});

// connect to MongoDB
const url = ``;
const db = async () => {
  try {
    await mongoose.connect(url);
    console.log('Connected to MongoDB');
  } catch (error) {
    console.error('Could not connect to MongoDB:', error);
  }
};
db();

// schema for db collection
const favoriteSchema = new mongoose.Schema({
  id: ObjectId,
  ticker: String,
  name: String,
});
const holdingSchema = new mongoose.Schema({
  id: ObjectId,
  ticker: String,
  name: String,
  quantity: Number,
  cost: Number, // total cost
});
const walletSchema = new mongoose.Schema({
  id: ObjectId,
  money: Number,
});

// Create model from schema
const Favorite = mongoose.model('Favorite', favoriteSchema, 'favorites'); // 3rd argument specifies the collection name
const Holding = mongoose.model('Holding', holdingSchema, 'holdings');
const Wallet = mongoose.model('Wallet', walletSchema, 'wallet');

// get favorites
app.get('/favorites', async (req, res) => {
  try {
    const favorites = await Favorite.find();
    res.json(favorites);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});
// update favorite
app.post('/favorites', async (req, res) => {
  // if favorite already exist, delete it
  try {
    const favorite = await Favorite.findOne({ ticker: req.body.ticker})
    if (favorite) {
      await favorite.deleteOne();
      res.json({ message: 'Favorite deleted' });
      return;
    }
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
  // add new favorite
  const newFavorite = new Favorite({ ticker: req.body.ticker, name: req.body.name});
  try {
    await newFavorite.save();
    res.status(201).json(newFavorite);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// get holding
app.get('/holdings', async (req, res) => {
  try {
    const holdings = await Holding.find();
    res.json(holdings);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});
// update holding
app.post('/holdings', async (req, res) => {
  try {
    let holding = await Holding.findOne({ticker: req.body.ticker});

    if (holding) {
      // If holding exists, update it
      const newQuantity = holding.quantity + req.body.quantity;

      // if new quantity is 0, delete the holding
      if (newQuantity === 0) {
        await holding.deleteOne();
        res.json({ message: 'Holding deleted' });
        return;
      }

      const newCost = holding.cost + req.body.cost;
      
      holding.quantity = newQuantity;
      holding.cost = newCost;
      
      await holding.save();
      res.status(200).json(holding);
    } else {
      // If holding does not exist, create a new one
      const newHolding = new Holding({
        ticker: req.body.ticker,
        name: req.body.name,
        quantity: req.body.quantity,
        cost: req.body.cost
      });
      await newHolding.save();
      res.status(201).json(newHolding);
    }
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// get wallet
app.get('/wallet', async (req, res) => {
  try {
    const wallet = await Wallet.findOne();
    res.json(wallet);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});
// update wallet
app.post('/wallet', async (req, res) => {
  try {
    const wallet = await Wallet.findOne();
    const newMoney = wallet.money + req.body.change;
    wallet.money = newMoney;
    await wallet.save();
    res.status(200).json(wallet);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

// app.get('/submit', (req, res) => {
//   res.sendFile(path.join(__dirname, '../client/src/index.html'));
// });
// app.post('/submit', (req, res) => {
//   console.log({
//     name: req.body.name,
//     message: req.body.message,
//   });
//   res.send('Thanks for your message!');
// });